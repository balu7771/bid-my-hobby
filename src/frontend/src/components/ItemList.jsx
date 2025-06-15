import { useState, useEffect } from 'react';
import { ENDPOINTS } from '../api/apiConfig';
import BidModal from './BidModal';
import ImageModal from './ImageModal';
import ItemActionModal from './ItemActionModal';
import BidAccessModal from './BidAccessModal';
import PublicBidsModal from './PublicBidsModal';
import { mockItems, getMockImageUrl } from './MockData';
import './ai-description.css';
import './public-bids.css';

// AI Description component
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

function ItemList() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedItem, setSelectedItem] = useState(null);
  const [selectedImage, setSelectedImage] = useState(null);
  const [actionItem, setActionItem] = useState(null);
  const [actionType, setActionType] = useState(null); // 'delete' or 'markSold'
  const [actionSuccess, setActionSuccess] = useState(null);
  const [useMockData, setUseMockData] = useState(false);
  const [bidAccessItem, setBidAccessItem] = useState(null);
  const [publicBidsItem, setPublicBidsItem] = useState(null);

  const fetchItems = async () => {
    try {
      const response = await fetch(ENDPOINTS.ITEMS);
      
      if (!response.ok) {
        throw new Error(`Error: ${response.status}`);
      }
      
      const data = await response.json();
      console.log("Fetched items:", data); // Debug log
      setItems(data);
    } catch (err) {
      console.warn('Error fetching items, using mock data:', err.message);
      setUseMockData(true);
      setItems(mockItems);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
  }, []);

  const handleBidClick = (item) => {
    if (item.status === 'SOLD') return; // Prevent bidding on sold items
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
    // Set success message based on action type or use provided message
    setActionSuccess(message || (actionType === 'delete' ? 'Item deleted successfully' : 'Item marked as sold'));
    
    // Refresh the items list after an action
    fetchItems();
    setActionItem(null);
    setActionType(null);
    
    // Clear success message after 3 seconds
    setTimeout(() => {
      setActionSuccess(null);
    }, 3000);
  };

  const handleBidPlaced = (itemId, bidAmount) => {
    // In a real app, you might want to update the UI to reflect the new bid
    console.log(`Bid placed on item ${itemId} for ${bidAmount}`);
  };
  
  // Helper function to mask email
  const maskEmail = (email) => {
    if (!email) return '';
    const [username, domain] = email.split('@');
    const maskedUsername = username.charAt(0) + '*'.repeat(username.length - 2) + username.charAt(username.length - 1);
    return `${maskedUsername}@${domain}`;
  };

  // Helper function to get currency symbol
  const getCurrencySymbol = (currency) => {
    switch(currency) {
      case 'USD': return '$';
      case 'GBP': return '£';
      case 'INR': return '₹';
      default: return '$';
    }
  };

  // Helper function to get image URL
  const getImageUrl = (item) => {
    if (useMockData) {
      return getMockImageUrl(item.itemId);
    }
    return item.url || ENDPOINTS.IMAGE(item.itemId);
  };

  // Filter out DELETED items
  const displayItems = items.filter(item => item.status !== 'DELETED');

  if (loading) return <div className="loading">Loading items...</div>;
  if (error) return <div className="error">Error loading items: {error}</div>;
  if (displayItems.length === 0) return <div className="no-items">No items available</div>;

  return (
    <div className="items-container">
      <h2>Available Items for Bidding</h2>
      {actionSuccess && <div className="success-message">{actionSuccess}</div>}
      <div className="items-grid">
        {displayItems.map((item) => (
          <div className={`item-card ${item.status === 'SOLD' ? 'item-sold' : ''}`} key={item.itemId}>
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
              {item.status === 'ACTIVE' && (
                <div className="status-badge active">ACTIVE</div>
              )}
            </div>
            <div className="item-details">
              <h3>{item.name}</h3>
              <p>{item.description}</p>
              {item.aiDescription && (
                <AiDescription description={item.aiDescription} />
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
                  disabled={item.status === 'SOLD'}
                >
                  {item.status === 'SOLD' ? 'Sold' : 'Place Bid'}
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
                
                {/* Show management buttons for the item creator */}
                {(item.email === localStorage.getItem('userEmail') || 
                  item.email === 'bidmyhobby@gmail.com' || 
                  item.userId === 'user123') && 
                  item.status === 'ACTIVE' && (
                  <div className="management-buttons">
                    <button 
                      className="view-bids-button"
                      onClick={() => setBidAccessItem(item)}
                    >
                      Detailed Bids
                    </button>
                    <button 
                      className="mark-sold-button"
                      onClick={() => handleActionClick(item, 'markSold')}
                    >
                      Mark as Sold
                    </button>
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
        <BidAccessModal
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

export default ItemList;