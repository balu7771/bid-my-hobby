import { useState, useEffect } from 'react';
import { ENDPOINTS } from '../api/apiConfig';
import BidModal from './BidModal';
import ImageModal from './ImageModal';
import ItemActionModal from './ItemActionModal';
import DetailedBidsModal from './DetailedBidsModal';
import PublicBidsModal from './PublicBidsModal';
import { mockItems, getMockImageUrl } from './MockData';
import './ai-description.css';
import './public-bids.css';
import './status-badges.css';
import './management-buttons.css';

function AiDescription({ description }) {
  const [expanded, setExpanded] = useState(false);
  
  return (
    <div className="ai-description">
      <div className="ai-description-title">
        <span className="ai-description-icon">🤖</span>
        AI Description
        <button 
          className="ai-description-toggle" 
          onClick={(e) => {
            e.stopPropagation();
            setExpanded(!expanded);
          }}
        >
          {expanded ? 'Show less' : 'Show more'}
        </button>
      </div>
      <div className={`ai-description-content ${expanded ? 'expanded' : ''}`}>
        {description}
      </div>
    </div>
  );
}

function EnhancedItemList() {
  const [items, setItems] = useState([]);
  const [filteredItems, setFilteredItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedItem, setSelectedItem] = useState(null);
  const [selectedImage, setSelectedImage] = useState(null);
  const [actionItem, setActionItem] = useState(null);
  const [actionType, setActionType] = useState(null);
  const [actionSuccess, setActionSuccess] = useState(null);
  const [useMockData, setUseMockData] = useState(false);
  const [bidAccessItem, setBidAccessItem] = useState(null);
  const [publicBidsItem, setPublicBidsItem] = useState(null);
  const [userEmail, setUserEmail] = useState('');
  const [showCreatorMode, setShowCreatorMode] = useState(false);
  const [creatorItems, setCreatorItems] = useState([]);

  // Filter states
  const [sortBy, setSortBy] = useState('newest');
  const [filterCategory, setFilterCategory] = useState('all');
  const [filterStatus, setFilterStatus] = useState('all');
  const [priceRange, setPriceRange] = useState({ min: '', max: '' });

  // Dynamic categories based on uploaded items
  const getAvailableCategories = () => {
    const uniqueCategories = [...new Set(items.map(item => item.category).filter(Boolean))];
    return ['all', ...uniqueCategories.sort()];
  };

  useEffect(() => {
    const email = localStorage.getItem('userEmail');
    if (email) {
      setUserEmail(email);
    }
    fetchItems();
  }, []);

  useEffect(() => {
    applyFilters();
  }, [items, sortBy, filterCategory, filterStatus, priceRange]);

  const fetchItems = async () => {
    try {
      const response = await fetch(ENDPOINTS.ITEMS);
      
      if (!response.ok) {
        throw new Error(`Error: ${response.status}`);
      }
      
      const data = await response.json();
      setItems(data);
    } catch (err) {
      console.warn('Error fetching items, using mock data:', err.message);
      setUseMockData(true);
      setItems(mockItems);
    } finally {
      setLoading(false);
    }
  };

  const fetchCreatorItems = async () => {
    if (!userEmail) return;
    
    try {
      const response = await fetch(`${ENDPOINTS.BASE}/creatorItems?email=${encodeURIComponent(userEmail)}`);
      if (response.ok) {
        const data = await response.json();
        setCreatorItems(data);
      }
    } catch (err) {
      console.error('Error fetching creator items:', err);
    }
  };

  const applyFilters = () => {
    let filtered = [...items];

    // Filter by category
    if (filterCategory !== 'all') {
      filtered = filtered.filter(item => item.category === filterCategory);
    }

    // Filter by status
    if (filterStatus !== 'all') {
      filtered = filtered.filter(item => {
        if (filterStatus === 'active') return item.status === 'ACTIVE';
        if (filterStatus === 'sold') return item.status === 'SOLD';
        if (filterStatus === 'not_for_sale') return item.status === 'NOT_FOR_SALE';
        if (filterStatus === 'sale_in_progress') return item.status === 'SALE_IN_PROGRESS';
        return true;
      });
    }

    // Filter by price range
    if (priceRange.min || priceRange.max) {
      filtered = filtered.filter(item => {
        const price = parseFloat(item.basePrice) || 0;
        const min = parseFloat(priceRange.min) || 0;
        const max = parseFloat(priceRange.max) || Infinity;
        return price >= min && price <= max;
      });
    }

    // Sort items
    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'likes':
          return (b.likes || 0) - (a.likes || 0);
        case 'newest':
          return (b.timestamp || 0) - (a.timestamp || 0);
        case 'oldest':
          return (a.timestamp || 0) - (b.timestamp || 0);
        case 'price_low':
          return (a.basePrice || 0) - (b.basePrice || 0);
        case 'price_high':
          return (b.basePrice || 0) - (a.basePrice || 0);
        case 'bids':
          return (b.bids?.length || 0) - (a.bids?.length || 0);
        default:
          return 0;
      }
    });

    setFilteredItems(filtered.filter(item => item.status !== 'DELETED'));
  };

  const handleLike = async (item, e) => {
    e.stopPropagation();
    
    // Optimistic update first
    const isLiked = item.likedByUser;
    const updateItem = (i) => i.itemId === item.itemId 
      ? { 
          ...i, 
          likes: isLiked ? Math.max(0, (i.likes || 1) - 1) : (i.likes || 0) + 1,
          likedByUser: !isLiked 
        }
      : i;
    
    setItems(prevItems => prevItems.map(updateItem));
    setFilteredItems(prevItems => prevItems.map(updateItem));
    
    try {
      const endpoint = isLiked ? 'unlikeItem' : 'likeItem';
      
      let identifier = userEmail;
      if (!identifier) {
        let guestId = localStorage.getItem('guestId');
        if (!guestId) {
          guestId = `guest_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
          localStorage.setItem('guestId', guestId);
        }
        identifier = guestId;
      }
      
      await fetch(`${ENDPOINTS.BASE}/${endpoint}/${item.itemId}?email=${encodeURIComponent(identifier)}`, {
        method: 'POST'
      });
    } catch (error) {
      console.error('Error liking item:', error);
      // Revert on error
      const revertItem = (i) => i.itemId === item.itemId 
        ? { ...i, likes: item.likes, likedByUser: item.likedByUser }
        : i;
      setItems(prevItems => prevItems.map(revertItem));
      setFilteredItems(prevItems => prevItems.map(revertItem));
    }
  };

  const handleBidClick = (item) => {
    if (item.status === 'SOLD' || item.status === 'NOT_FOR_SALE' || item.status === 'SALE_IN_PROGRESS') return;
    setSelectedItem(item);
  };

  const handleImageClick = (item) => {
    setSelectedImage(item);
  };

  const handleActionClick = (item, type) => {
    setActionItem(item);
    setActionType(type);
  };

  const handleActionComplete = (message) => {
    setActionSuccess(message || (actionType === 'delete' ? 'Item deleted successfully' : 'Item updated successfully'));
    fetchItems();
    if (showCreatorMode) fetchCreatorItems();
    setActionItem(null);
    setActionType(null);
    
    setTimeout(() => {
      setActionSuccess(null);
    }, 3000);
  };

  const handleBidPlaced = (itemId, bidAmount) => {
    console.log(`Bid placed on item ${itemId} for ${bidAmount}`);
  };

  const maskEmail = (email) => {
    if (!email) return '';
    const [username, domain] = email.split('@');
    const maskedUsername = username.charAt(0) + '*'.repeat(username.length - 2) + username.charAt(username.length - 1);
    return `${maskedUsername}@${domain}`;
  };

  const getCurrencySymbol = (currency) => {
    switch(currency) {
      case 'USD': return '$';
      case 'GBP': return '£';
      case 'INR': return '₹';
      default: return '$';
    }
  };

  const getImageUrl = (item) => {
    if (useMockData) {
      return getMockImageUrl(item.itemId);
    }
    return item.url || ENDPOINTS.IMAGE(item.itemId);
  };

  const toggleCreatorMode = () => {
    if (!showCreatorMode && userEmail) {
      fetchCreatorItems();
    }
    setShowCreatorMode(!showCreatorMode);
  };

  const displayItems = showCreatorMode ? creatorItems : filteredItems;

  if (loading) return <div className="loading">Loading items...</div>;
  if (error) return <div className="error">Error loading items: {error}</div>;

  return (
    <div className="items-container">
      <div className="items-header">
        <h2>{showCreatorMode ? 'My Items' : 'Available Items for Bidding'}</h2>
        
        {userEmail && (
          <div className="creator-controls">
            <button 
              className={`creator-toggle ${showCreatorMode ? 'active' : ''}`}
              onClick={toggleCreatorMode}
            >
              {showCreatorMode ? 'View All Items' : 'My Items'}
            </button>
            <span className="creator-email">Creator: {maskEmail(userEmail)}</span>
          </div>
        )}
      </div>

      {!showCreatorMode && (
        <div className="filters-container desktop-only">
          <div className="filter-group">
            <label>Sort by:</label>
            <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="likes">Most Liked</option>
              <option value="newest">Newest First</option>
              <option value="oldest">Oldest First</option>
              <option value="price_low">Price: Low to High</option>
              <option value="price_high">Price: High to Low</option>
              <option value="bids">Most Bids</option>
            </select>
          </div>

          <div className="filter-group">
            <label>Category:</label>
            <select value={filterCategory} onChange={(e) => setFilterCategory(e.target.value)}>
              {getAvailableCategories().map(cat => (
                <option key={cat} value={cat}>
                  {cat === 'all' ? 'All Categories' : cat}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label>Status:</label>
            <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
              <option value="all">All Items</option>
              <option value="active">Available</option>
              <option value="sold">Sold</option>
              <option value="not_for_sale">Not for Sale</option>
              <option value="sale_in_progress">Sale in Progress</option>
            </select>
          </div>

          <div className="filter-group price-range">
            <label>Price Range:</label>
            <input
              type="number"
              placeholder="Min"
              value={priceRange.min}
              onChange={(e) => setPriceRange(prev => ({ ...prev, min: e.target.value }))}
            />
            <span>-</span>
            <input
              type="number"
              placeholder="Max"
              value={priceRange.max}
              onChange={(e) => setPriceRange(prev => ({ ...prev, max: e.target.value }))}
            />
          </div>
        </div>
      )}

      {actionSuccess && <div className="success-message">{actionSuccess}</div>}
      
      {displayItems.length === 0 ? (
        <div className="no-items">
          {showCreatorMode ? 'You haven\'t created any items yet' : 'No items match your filters'}
        </div>
      ) : (
        <div className="items-grid">
          {displayItems.map((item) => (
            <div className={`item-card ${item.status === 'SOLD' ? 'item-sold' : ''} ${item.status === 'NOT_FOR_SALE' ? 'item-not-for-sale' : ''} ${item.status === 'SALE_IN_PROGRESS' ? 'item-sale-in-progress' : ''}`} key={item.itemId}>
              <div className="thumbnail-container" onClick={() => handleImageClick(item)}>
                <div className="watermarked-image-container">
                  <img 
                    src={getImageUrl(item)} 
                    alt={item.name} 
                    className="item-image"
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = 'https://via.placeholder.com/150?text=No+Image';
                    }}
                  />
                  <div className="watermark">Bid My Hobby</div>
                  {item.email && (
                    <div className="email-watermark">
                      {maskEmail(item.email)}
                    </div>
                  )}
                  {item.bids && item.bids.length > 0 && (
                    <div className="bid-badge">{item.bids.length}</div>
                  )}
                </div>
                <div className="thumbnail-overlay">
                  <span className="view-icon">🔍</span>
                </div>
                {item.status === 'SOLD' && (
                  <div className="status-badge sold">SOLD</div>
                )}
                {item.status === 'NOT_FOR_SALE' && (
                  <div className="status-badge not-for-sale">NOT FOR SALE</div>
                )}
                {item.status === 'ACTIVE' && (
                  <div className="status-badge active">ACTIVE</div>
                )}
                {item.status === 'SALE_IN_PROGRESS' && (
                  <div className="status-badge sale-in-progress">SALE IN PROGRESS</div>
                )}
              </div>
              
              <div className="item-details">
                <div className="item-header">
                  <h3>{item.name}</h3>
                  <div className="item-actions-top">
                    <button 
                      className={`like-button ${item.likedByUser ? 'liked' : ''}`}
                      onClick={(e) => handleLike(item, e)}
                      title={item.likedByUser ? 'Unlike' : 'Like'}
                    >
                      ❤️ {item.likes || 0}
                    </button>
                  </div>
                </div>
                
                {item.category && (
                  <div className="item-category">
                    <span className="category-tag">{item.category}</span>
                  </div>
                )}
                
                <p>{item.description}</p>
                
                {(item.city || item.instagram || item.website) && (
                  <div className="creator-info">
                    {item.city && <span className="creator-city">📍 {item.city}</span>}
                    {item.instagram && <a href={item.instagram} target="_blank" rel="noopener noreferrer" className="creator-link">📷 Instagram</a>}
                    {item.website && <a href={item.website} target="_blank" rel="noopener noreferrer" className="creator-link">🌐 Website</a>}
                  </div>
                )}
                
                {item.aiDescription && (
                  <AiDescription description={item.aiDescription} />
                )}
                
                {item.estimatedPrice && (
                  <div className="ai-price-estimate">
                    <span className="ai-icon">🤖</span>
                    AI Estimated: {getCurrencySymbol(item.currency)}{item.estimatedPrice}
                  </div>
                )}
                
                {item.basePrice && (
                  <div className="price-display">
                    <span className="currency-symbol">
                      {getCurrencySymbol(item.currency)}
                    </span>
                    {item.basePrice} {item.currency}
                  </div>
                )}
                
                <div className="item-actions">
                  <button 
                    className="bid-button"
                    onClick={() => handleBidClick(item)}
                    disabled={item.status === 'SOLD' || item.status === 'NOT_FOR_SALE' || item.status === 'SALE_IN_PROGRESS'}
                  >
                    {item.status === 'SOLD' ? 'Sold' : 
                     item.status === 'NOT_FOR_SALE' ? 'Not for Sale' : 
                     item.status === 'SALE_IN_PROGRESS' ? 'Sale in Progress' : 'Place Bid'}
                  </button>
                  
                  <button 
                    className="view-bids-button"
                    onClick={(e) => {
                      e.stopPropagation();
                      setPublicBidsItem(item);
                    }}
                  >
                    View Bids
                    {item.bids && item.bids.length > 0 && (
                      <span className="bid-badge">{item.bids.length}</span>
                    )}
                  </button>
                  
                  {showCreatorMode && userEmail && userEmail === item.email && (
                    <div className="management-buttons">
                      <button 
                        className="view-bids-button"
                        onClick={() => setBidAccessItem(item)}
                      >
                        Detailed Bids
                      </button>
                      
                      {item.status === 'ACTIVE' && (
                        <>
                          <button 
                            className="mark-sold-button"
                            onClick={() => handleActionClick(item, 'markSold')}
                          >
                            Mark as Sold
                          </button>
                          <button 
                            className="mark-not-for-sale-button"
                            onClick={() => handleActionClick(item, 'markNotForSale')}
                          >
                            Mark as Not for Sale
                          </button>
                        </>
                      )}
                      
                      <button 
                        className="delete-button"
                        onClick={() => handleActionClick(item, 'delete')}
                      >
                        Delete Item
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {selectedItem && (
        <BidModal 
          item={selectedItem} 
          onClose={() => setSelectedItem(null)} 
          onBidPlaced={handleBidPlaced}
        />
      )}

      {selectedImage && (
        <ImageModal 
          imageUrl={getImageUrl(selectedImage)}
          alt={selectedImage.name}
          onClose={() => setSelectedImage(null)}
        />
      )}

      {actionItem && (
        <ItemActionModal
          item={actionItem}
          actionType={actionType}
          onClose={() => { setActionItem(null); setActionType(null); }}
          onActionComplete={handleActionComplete}
        />
      )}

      {bidAccessItem && (
        <DetailedBidsModal
          item={bidAccessItem}
          onClose={() => setBidAccessItem(null)}
        />
      )}

      {publicBidsItem && (
        <PublicBidsModal
          item={publicBidsItem}
          onClose={() => setPublicBidsItem(null)}
        />
      )}
    </div>
  );
}

export default EnhancedItemList;