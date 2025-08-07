import { useState, useEffect } from 'react';
import { ENDPOINTS } from '../api/apiConfig';
import BidApprovalModal from './BidApprovalModal';
import './bid-approval.css';
import './detailed-bids.css';

function DetailedBidsModal({ item, onClose }) {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [bids, setBids] = useState([]);
  const [verified, setVerified] = useState(false);
  const [userEmail, setUserEmail] = useState('');
  const [selectedBid, setSelectedBid] = useState(null);

  useEffect(() => {
    const savedEmail = localStorage.getItem('userEmail');
    if (savedEmail) {
      setUserEmail(savedEmail);
      setEmail(savedEmail);
      // Auto-verify if user email matches creator email
      if (savedEmail === item.email) {
        handleAutoVerify(savedEmail);
      }
    }
  }, [item.email]);

  const handleAutoVerify = async (emailToVerify) => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`/api/bid/getBids?itemId=${encodeURIComponent(item.itemId)}&email=${encodeURIComponent(emailToVerify)}`);
      
      if (!response.ok) {
        throw new Error(`Error: ${response.status}`);
      }
      
      const data = await response.json();
      const bidsList = Array.isArray(data) ? data : (data.bids || []);
      
      setBids(bidsList);
      setVerified(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!email) {
      setError('Please enter your email address');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Check if the entered email matches the creator's email
      if (email === item.email) {
        await handleAutoVerify(email);
      } else {
        throw new Error('Email does not match the creator\'s email');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
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
  
  // Format date
  const formatDate = (timestamp) => {
    return new Date(timestamp).toLocaleString();
  };

  return (
    <>
      <div className="modal-backdrop">
        <div className="modal-content">
          <div className="modal-header">
            <h3>Detailed Bids for {item.name}</h3>
            <button className="close-button" onClick={onClose}>×</button>
          </div>
        
        <div className="modal-body">
          {error && <div className="error-message">{error}</div>}
          
          {verified ? (
            bids.length === 0 ? (
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
                    <div className="bid-actions">
                      <button 
                        className="approve-bid-button"
                        onClick={() => {
                          console.log('Approve bid button clicked for:', bid);
                          setSelectedBid(bid);
                        }}
                        style={{
                          backgroundColor: '#27ae60',
                          color: 'white',
                          fontWeight: 'bold',
                          padding: '8px 16px',
                          fontSize: '14px',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
                        }}
                      >
                        ✓ Approve Bid
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )
          ) : (
            userEmail && userEmail === item.email ? (
              <div className="loading-message">Loading your bids...</div>
            ) : (
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label htmlFor="creatorEmail">Enter your email to verify:</label>
                  <input
                    type="email"
                    id="creatorEmail"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Enter your email address"
                    required
                  />
                  <small className="form-note">
                    If your email matches the creator's email, you'll see detailed bids with unmasked emails
                  </small>
                </div>
                
                <div className="modal-actions">
                  <button type="button" onClick={onClose} disabled={loading}>
                    Cancel
                  </button>
                  <button type="submit" disabled={loading}>
                    {loading ? 'Verifying...' : 'View Detailed Bids'}
                  </button>
                </div>
              </form>
            )
          )}
        </div>
      </div>
      </div>
      
      {selectedBid && (
        <BidApprovalModal
          item={item}
          bid={selectedBid}
          onClose={() => setSelectedBid(null)}
          onSuccess={() => {
            // Refresh bids after approval
            handleAutoVerify(userEmail);
          }}
        />
      )}
    </>
  );
}

export default DetailedBidsModal;