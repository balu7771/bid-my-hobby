import React, { useState } from 'react';

const PaymentModal = ({ isOpen, onClose, paymentData, onSuccess }) => {
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
            const result = await fetch('/api/payment/success', {
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
          name: 'Bidder',
          email: paymentData.bidderEmail || 'bidder@example.com',
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

      console.log('Opening Razorpay with live keys');
      const rzp = new window.Razorpay(options);
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
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white p-6 rounded-lg max-w-md w-full mx-4">
        <h2 className="text-xl font-bold mb-4">Payment Required</h2>
        <div className="mb-4">
          <p className="text-gray-600">{paymentData.description}</p>
          <p className="text-2xl font-bold text-green-600 mt-2">
            ₹{paymentData.amount}
          </p>
        </div>
        <div className="flex gap-3">
          <button
            onClick={handlePayment}
            disabled={loading}
            className="flex-1 bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 disabled:opacity-50"
          >
            {loading ? 'Processing...' : 'Pay Now'}
          </button>
          <button
            onClick={onClose}
            className="flex-1 bg-gray-300 text-gray-700 px-4 py-2 rounded hover:bg-gray-400"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentModal;