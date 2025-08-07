import React, { useState, useEffect } from 'react';
import { ENDPOINTS } from '../api/apiConfig';
import CreatorPaymentModal from './CreatorPaymentModal';
import './creator-payment.css';

const BidApprovalModal = ({ item, bid, onClose, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [paymentData, setPaymentData] = useState(null);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [userEmail, setUserEmail] = useState('');

  useEffect(() => {
    const savedEmail = localStorage.getItem('userEmail');
    if (savedEmail) {
      setUserEmail(savedEmail);
    }
  }, []);

  const handleApproveBid = async () => {
    if (!userEmail) {
      setError('Please set your email first');
      return;
    }

    setLoading(true);
    setError(null);
    
    console.log('Approving bid:', {
      itemId: item.itemId,
      creatorEmail: userEmail,
      bidderEmail: bid.userId,
      bidAmount: bid.bidAmount
    });

    try {
      // First, approve the bid
      const approveResponse = await fetch(ENDPOINTS.APPROVE_BID, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          itemId: item.itemId,
          creatorEmail: userEmail,
          bidderEmail: bid.userId,
          bidAmount: bid.bidAmount
        }),
      });
      
      console.log('Approve bid response status:', approveResponse.status);

      if (!approveResponse.ok) {
        const errorData = await approveResponse.text();
        throw new Error(errorData || 'Failed to approve bid');
      }

      // Check if platform fee is required (for amounts > Rs. 500)
      if (bid.bidAmount > 500) {
        console.log('Bid amount > 500, creating payment order');
        try {
          // Create payment order for platform fee
          const paymentResponse = await fetch(ENDPOINTS.CREATE_CREATOR_PAYMENT, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              itemId: item.itemId,
              creatorEmail: userEmail
            }),
          });
          
          console.log('Payment response status:', paymentResponse.status);

          if (paymentResponse.ok) {
            const paymentOrderData = await paymentResponse.json();
            console.log('Payment order data:', paymentOrderData);
            setPaymentData(paymentOrderData);
            setShowPaymentModal(true);
          } else {
            const errorText = await paymentResponse.text();
            console.error('Payment creation failed:', errorText);
            // If payment creation fails, still consider the bid approved
            onSuccess();
            onClose();
          }
        } catch (paymentError) {
          console.error('Error creating payment:', paymentError);
          // If payment creation fails, still consider the bid approved
          onSuccess();
          onClose();
        }
      } else {
        // No platform fee required
        onSuccess();
        onClose();
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handlePaymentSuccess = () => {
    onSuccess();
    onClose();
  };

  return (
    <>
      <div className="modal-backdrop" style={{zIndex: 1000}}>
        <div className="modal-content" style={{maxWidth: '500px', margin: '0 auto'}}>
          <div className="modal-header">
            <h3>Approve Bid</h3>
            <button className="close-button" onClick={onClose}>×</button>
          </div>
          
          <div className="modal-body">
            {error && <div className="error-message">{error}</div>}
            
            <div className="bid-approval-details">
              <p>You are about to approve the following bid:</p>
              
              <div className="bid-info">
                <div className="bid-info-row">
                  <span className="bid-info-label">Item:</span>
                  <span className="bid-info-value">{item.name}</span>
                </div>
                <div className="bid-info-row">
                  <span className="bid-info-label">Bidder:</span>
                  <span className="bid-info-value">{bid.userId}</span>
                </div>
                <div className="bid-info-row">
                  <span className="bid-info-label">Bid Amount:</span>
                  <span className="bid-info-value">₹{bid.bidAmount}</span>
                </div>
              </div>
              
              {bid.bidAmount > 500 && (
                <div className="platform-fee-notice">
                  <p>A platform fee of 5% (minimum ₹50) will be charged for this transaction.</p>
                  <p>You'll be redirected to the payment page after approving this bid.</p>
                </div>
              )}
            </div>
            
            <div className="modal-actions">
              <button
                onClick={onClose}
                className="creator-payment-btn secondary"
              >
                Cancel
              </button>
              <button
                onClick={handleApproveBid}
                disabled={loading}
                className="creator-payment-btn primary"
                style={{
                  backgroundColor: '#27ae60',
                  color: 'white',
                  fontWeight: 'bold',
                  padding: '12px 24px',
                  fontSize: '16px'
                }}
              >
                {loading ? 'Processing...' : '✓ Approve Bid'}
              </button>
            </div>
          </div>
        </div>
      </div>
      
      {showPaymentModal && paymentData && (
        <CreatorPaymentModal
          isOpen={showPaymentModal}
          onClose={() => setShowPaymentModal(false)}
          paymentData={paymentData}
          onSuccess={handlePaymentSuccess}
        />
      )}
    </>
  );
};

export default BidApprovalModal;