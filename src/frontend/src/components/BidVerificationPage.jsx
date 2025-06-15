import { useState, useEffect } from 'react';
import { ENDPOINTS } from '../api/apiConfig';

function BidVerificationPage() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [itemData, setItemData] = useState(null);
  const [bids, setBids] = useState([]);

  useEffect(() => {
    const fetchBidData = async () => {
      try {
        // Extract token from URL
        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');
        
        if (!token) {
          throw new Error('Verification token is missing');
        }

        const response = await fetch(ENDPOINTS.VERIFY_AND_GET_BIDS(token));
        
        if (!response.ok) {
          throw new Error(`Verification failed: ${response.status}`);
        }
        
        const data = await response.json();
        setItemData(data.item);
        setBids(data.bids || []);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchBidData();
  }, []);

  // Helper function to get currency symbol
  const getCurrencySymbol = (currency) => {
    switch(currency) {
      case 'USD': return '$';
      case 'GBP': return '£';
      case 'INR': return '₹';
      default: return '$';
    }
  };

  if (loading) return <div className="loading">Verifying your access...</div>;
  if (error) return <div className="error-container">Error: {error}</div>;
  if (!itemData) return <div className="error-container">No item data found</div>;

  return (
    <div className="bid-verification-container">
      <div className="verification-header">
        <h2>Bids for: {itemData.name}</h2>
        <div className="item-details">
          <p><strong>Description:</strong> {itemData.description}</p>
          <p><strong>Base Price:</strong> {getCurrencySymbol(itemData.currency)}{itemData.basePrice} {itemData.currency}</p>
          <p><strong>Status:</strong> {itemData.status}</p>
        </div>
      </div>

      {bids.length === 0 ? (
        <div className="no-bids-message">No bids have been placed on this item yet.</div>
      ) : (
        <div className="bids-table-container">
          <table className="bids-table">
            <thead>
              <tr>
                <th>Bidder Email</th>
                <th>Bid Amount</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {bids.map((bid, index) => (
                <tr key={index}>
                  <td>{bid.email}</td>
                  <td>
                    {getCurrencySymbol(itemData.currency)}
                    {bid.bidAmount} {itemData.currency}
                  </td>
                  <td>{new Date(bid.timestamp).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="back-link">
        <a href="/">Back to Items</a>
      </div>
    </div>
  );
}

export default BidVerificationPage;