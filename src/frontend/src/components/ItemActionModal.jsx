import { useState } from 'react';
import { ENDPOINTS } from '../api/apiConfig';

function ItemActionModal({ item, actionType, onClose, onActionComplete }) {
  const [email, setEmail] = useState(item.email || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!email) {
      setError('Please enter your email for verification');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      let response;
      const queryParams = new URLSearchParams({ email });
      
      if (actionType === 'delete') {
        // Extract just the filename part from the path
        const fullPath = item.itemId;
        const filename = fullPath.split('/').pop();
        console.log('Deleting item with ID:', filename, 'from full path:', fullPath);
        response = await fetch(ENDPOINTS.DELETE_ITEM(filename) + `?${queryParams}`, {
          method: 'DELETE'
        });
      } else if (actionType === 'markSold') {
        // Extract just the filename part from the path
        const fullPath = item.itemId;
        const filename = fullPath.split('/').pop();
        console.log('Marking item as sold with ID:', filename, 'from full path:', fullPath);
        response = await fetch(ENDPOINTS.MARK_AS_SOLD(filename) + `?${queryParams}`, {
          method: 'POST'
        });
      }

      if (!response.ok) {
        const text = await response.text();
        console.log(`Error response (${response.status}):`, text);
        
        // Handle specific error messages based on status code
        if (response.status === 404) {
          throw new Error('Item not found');
        } else if (response.status === 403) {
          throw new Error('Only the creator can perform this action');
        } else {
          throw new Error(text || `Failed to ${actionType === 'delete' ? 'delete' : 'mark as sold'}`);
        }
      }

      const responseText = await response.text();
      onActionComplete(responseText);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const actionTitle = actionType === 'delete' ? 'Delete Item' : 'Mark Item as Sold';
  const actionButtonText = actionType === 'delete' ? 'Delete' : 'Mark as Sold';
  const actionDescription = actionType === 'delete' 
    ? 'This will permanently remove your item from the marketplace.' 
    : 'This will mark your item as sold and prevent further bidding.';

  return (
    <div className="modal-backdrop">
      <div className="modal-content">
        <div className="modal-header">
          <h3>{actionTitle}: {item.name}</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-body">
          <p className="action-description">{actionDescription}</p>
          
          {error && <div className="error-message">{error}</div>}
          
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="verifyEmail">
                Verify your email:
              </label>
              <input
                type="email"
                id="verifyEmail"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email for verification"
                required
              />
              <small className="form-note">
                Please enter the email you used when creating this item
              </small>
            </div>
            
            <div className="modal-actions">
              <button type="button" onClick={onClose} disabled={loading}>
                Cancel
              </button>
              <button 
                type="submit" 
                disabled={loading}
                className={actionType === 'delete' ? 'delete-button' : 'mark-sold-button'}
              >
                {loading ? 'Processing...' : actionButtonText}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default ItemActionModal;