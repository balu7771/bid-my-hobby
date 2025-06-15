import { useState } from 'react';
import { ENDPOINTS } from '../api/apiConfig';

function BidAccessModal({ item, onClose }) {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!email) {
      setError('Please enter your email address');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`${ENDPOINTS.REQUEST_BID_ACCESS(item.itemId)}?email=${encodeURIComponent(email)}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to request bid access');
      }

      setSuccess(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-content">
        <div className="modal-header">
          <h3>View Bids for {item.name}</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-body">
          {error && <div className="error-message">{error}</div>}
          
          {success ? (
            <div className="success-container">
              <div className="success-message">
                Verification email sent! Please check your inbox and click the link to view all bids.
              </div>
              <button onClick={onClose} className="close-success-button">
                Close
              </button>
            </div>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="creatorEmail">Verify you're the creator:</label>
                <input
                  type="email"
                  id="creatorEmail"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Enter your email address"
                  required
                />
                <small className="form-note">
                  We'll send a verification link to this email if it matches the creator's email
                </small>
              </div>
              
              <div className="modal-actions">
                <button type="button" onClick={onClose} disabled={loading}>
                  Cancel
                </button>
                <button type="submit" disabled={loading}>
                  {loading ? 'Sending...' : 'Request Access'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

export default BidAccessModal;