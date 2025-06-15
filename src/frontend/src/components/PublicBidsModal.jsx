import { useState, useEffect } from 'react';
import { ENDPOINTS } from '../api/apiConfig';

function PublicBidsModal({ item, onClose }) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [bids, setBids] = useState([]);
  
  useEffect(() => {
    const fetchBids = async () => {
      try {
        const response = await fetch(ENDPOINTS.GET_BIDS(item.itemId));
        
        if (!response.ok) {
          throw new Error(`Error: ${response.status}`);
        }
        
        const data = await response.json();
        setBids(data.bids || []);
      } catch (err) {
        console.error('Error fetching bids:', err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    
    fetchBids();
  }, [item.itemId]);
  
  // Helper function to get currency symbol
  const getCurrencySymbol = (currency) => {
    switch(currency) {
      case 'USD': return '$';
      case 'GBP': return '£';
      case 'INR': return '₹';
      default: return '$';
    }
  };
  
  // Format date
  const formatDate = (timestamp) => {
    return new Date(timestamp).toLocaleString();
  };
  
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Bids for {item.name}</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-body">
          {loading ? (
            <div className="loading-spinner">Loading bids...</div>
          ) : error ? (
            <div className="error-message">Error loading bids: {error}</div>
          ) : bids.length === 0 ? (
            <div className="no-bids-message">No bids have been placed on this item yet.</div>
          ) : (
            <div className="bids-list">
              <div className="bid-count">
                <span className="bid-count-number">{bids.length}</span> 
                {bids.length === 1 ? 'bid' : 'bids'} on this item
              </div>
              
              {bids.map((bid, index) => (
                <div key={index} className="bid-item">
                  <div className="bid-amount">
                    <span className="currency-symbol">
                      {getCurrencySymbol(bid.currency)}
                    </span>
                    {bid.bidAmount} {bid.currency}
                  </div>
                  <div className="bid-details">
                    <div className="bid-user">{bid.userId}</div>
                    <div className="bid-time">{formatDate(bid.timestamp)}</div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default PublicBidsModal;