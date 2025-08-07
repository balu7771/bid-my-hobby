import React, { useState } from 'react';
import './creator-payment.css';

const CreatorPaymentModal = ({ isOpen, onClose, paymentData, onSuccess }) => {
  const [loading, setLoading] = useState(false);

  const handlePayment = async () => {
    setLoading(true);
    
    try {
      // Check if Razorpay is loaded
      if (!window.Razorpay) {
        throw new Error('Razorpay SDK not loaded');
      }
      
      // Validate payment data
      if (!paymentData.keyId || !paymentData.orderId) {
        throw new Error('Invalid payment configuration');
      }
      
      const options = {
        key: paymentData.keyId,
        amount: paymentData.amount * 100, // Amount in paise
        currency: paymentData.currency,
        name: 'Bid My Hobby',
        description: paymentData.description,
        order_id: paymentData.orderId,
        handler: async function (response) {
          try {
            console.log('Payment successful, verifying with backend...');
            // Send payment success to backend
            const result = await fetch('/api/payment/creatorPaymentSuccess', {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
              },
              body: JSON.stringify({
                razorpay_payment_id: response.razorpay_payment_id,
                razorpay_order_id: response.razorpay_order_id,
                razorpay_signature: response.razorpay_signature,
              }),
            });

            if (result.ok) {
              onSuccess();
              onClose();
            } else {
              const errorData = await result.text();
              console.error('Payment verification failed:', errorData);
              alert('Payment verification failed: ' + errorData);
            }
          } catch (error) {
            console.error('Payment verification error:', error);
            alert('Payment verification failed: ' + error.message);
          }
        },
        prefill: {
          name: 'Creator',
          email: paymentData.creatorEmail || 'creator@example.com',
        },
        theme: {
          color: '#3399cc',
        },
        modal: {
          ondismiss: function() {
            setLoading(false);
          }
        }
      };

      console.log('Opening Razorpay with live keys', options);
      const rzp = new window.Razorpay(options);
      rzp.on('payment.failed', function(response){
        console.error('Payment failed:', response.error);
        alert('Payment failed: ' + response.error.description);
        setLoading(false);
      });
      rzp.open();
    } catch (error) {
      console.error('Payment error:', error);
      alert('Payment failed: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="creator-payment-modal" style={{zIndex: 2000}}>
      <div className="creator-payment-content" style={{maxWidth: '500px', margin: '0 auto', boxShadow: '0 4px 12px rgba(0,0,0,0.2)'}}>
        <div className="creator-payment-header">
          <h2>Platform Fee Payment</h2>
          <p className="creator-payment-description">{paymentData.description}</p>
        </div>
        
        <div className="creator-payment-amount">
          ₹{paymentData.amount}
        </div>
        
        <p className="creator-payment-description">
          This fee helps us maintain the platform and provide better services.
        </p>
        
        <div className="creator-payment-actions">
          <button
            onClick={handlePayment}
            disabled={loading}
            className="creator-payment-btn primary"
          >
            {loading ? 'Processing...' : 'Pay Platform Fee'}
          </button>
          <button
            onClick={onClose}
            className="creator-payment-btn secondary"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default CreatorPaymentModal;