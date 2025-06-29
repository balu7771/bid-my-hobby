import { useState, useEffect } from 'react';
import { ENDPOINTS } from '../api/apiConfig';
import CreatorPaymentModal from './CreatorPaymentModal';
import './mobile-upload.css';

function ItemUpload() {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [email, setEmail] = useState('');
  const [basePrice, setBasePrice] = useState('');
  const [currency, setCurrency] = useState('INR');
  const [city, setCity] = useState('');
  const [instagram, setInstagram] = useState('');
  const [website, setWebsite] = useState('');
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [paymentData, setPaymentData] = useState(null);
  const [uploadedItemId, setUploadedItemId] = useState(null);

  // Auto-populate email from localStorage
  useEffect(() => {
    const savedEmail = localStorage.getItem('userEmail');
    if (savedEmail) {
      setEmail(savedEmail);
    }
  }, []);

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    setFile(selectedFile);
    
    // Create preview for the selected image
    if (selectedFile) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreview(reader.result);
      };
      reader.readAsDataURL(selectedFile);
    } else {
      setPreview(null);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!file || !title || !email || !basePrice || !currency) {
      setMessage('Please fill all required fields and select an image');
      return;
    }

    // Check price limits
    const price = parseFloat(basePrice);
    if (price > 10000) {
      setMessage('Error: Maximum price allowed is ₹10,000. Please reduce the price.');
      return;
    }

    setLoading(true);
    setMessage('');
    
    // Inform user about content moderation
    setMessage('Uploading and analyzing image...');

    // According to Swagger, the file should be in the request body as JSON
    // But multipart/form-data is more appropriate for file uploads
    const formData = new FormData();
    formData.append('file', file);
    
    // According to Swagger, these are query parameters
    const queryParams = new URLSearchParams({
      name: title,
      description: description,
      userId: 'user123', // In a real app, this would come from authentication
      email: email,
      basePrice: basePrice,
      currency: currency
    });
    
    if (city.trim()) queryParams.append('city', city.trim());
    if (instagram.trim()) queryParams.append('instagram', instagram.trim());
    if (website.trim()) queryParams.append('website', website.trim());

    try {
      const response = await fetch(`${ENDPOINTS.UPLOAD_ITEM}?${queryParams}`, {
        method: 'POST',
        body: formData,
        // Don't set Content-Type header, browser will set it with boundary for multipart/form-data
      });

      let data;
      const contentType = response.headers.get('content-type');
      
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        // Handle non-JSON responses
        const text = await response.text();
        data = { message: text };
      }
      
      if (response.ok) {
        let responseData;
        try {
          responseData = await response.json();
        } catch (e) {
          responseData = { message: 'Item uploaded successfully', itemId: 'unknown' };
        }
        const price = parseFloat(basePrice);
        
        setUploadedItemId(responseData.itemId);
        
        // Check if platform fee is required (3000-10000 INR)
        if (responseData.requiresPlatformFee) {
          setMessage(`Item uploaded successfully! ${responseData.feeMessage}`);
          
          // Show payment modal for platform fee
          setTimeout(() => {
            handlePlatformFeePayment(responseData.itemId);
          }, 2000);
        } else {
          setMessage('Item uploaded successfully!');
          clearForm();
        }
      } else {
        // Handle specific error messages for image moderation
        if (data.message && data.message.includes('inappropriate content')) {
          setMessage('Error: The image contains inappropriate content and cannot be uploaded.');
        } else if (data.message && data.message.includes('not appear to be a hobby item')) {
          setMessage('Error: The image does not appear to be a hobby item. Only hobby items can be uploaded.');
        } else if (data.error && data.error.includes('Price validation failed')) {
          setMessage(`Error: ${data.reason}`);
        } else {
          setMessage(`Error: ${data.message || data.error || 'Failed to upload item'}`);
        }
      }
    } catch (error) {
      setMessage(`Error: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const clearForm = () => {
    setTitle('');
    setDescription('');
    setEmail('');
    setBasePrice('');
    setCurrency('INR');
    setCity('');
    setInstagram('');
    setWebsite('');
    setFile(null);
    setPreview(null);
  };

  const handlePlatformFeePayment = async (itemId) => {
    try {
      const response = await fetch('/api/payment/createCreatorPayment', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          itemId: itemId,
          creatorEmail: email
        })
      });

      if (response.ok) {
        const paymentOrder = await response.json();
        setPaymentData({
          ...paymentOrder,
          creatorEmail: email
        });
        setShowPaymentModal(true);
      } else {
        const errorText = await response.text();
        setMessage(`Error creating payment order: ${errorText}`);
      }
    } catch (error) {
      setMessage(`Error: ${error.message}`);
    }
  };

  const handlePaymentSuccess = () => {
    setMessage('Platform fee payment successful! Your item is now fully active.');
    clearForm();
    setShowPaymentModal(false);
    setPaymentData(null);
    setUploadedItemId(null);
  };

  const handlePaymentCancel = () => {
    setShowPaymentModal(false);
    setPaymentData(null);
    setMessage('Payment cancelled. Your item is uploaded but platform fee is pending.');
  };

  return (
    <div className="upload-container">
      <h2>Share Your Hobby Creation</h2>
      {message && <div className={message.includes('Error') ? 'error-message' : 'success-message'}>{message}</div>}
      
      <form onSubmit={handleSubmit}>
        {/* Mandatory Fields */}
        <div className="form-group">
          <label htmlFor="title">Title: *</label>
          <input
            type="text"
            id="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Name your creation"
            required
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="basePrice">Base Price (INR only): *</label>
          <input
            type="number"
            id="basePrice"
            value={basePrice}
            onChange={(e) => setBasePrice(e.target.value)}
            placeholder="Starting price in ₹"
            min="100"
            max="10000"
            step="50"
            required
          />
          <small className="form-note">
            Note: Maximum price allowed is ₹10,000. Items between ₹3,000-₹10,000 require a 10% platform fee.
            Price will be validated against AI estimate. USD & GBP equivalents will be shown for reference.
          </small>
        </div>
        
        <div className="form-group">
          <label htmlFor="email">Email: *</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Your email for notifications"
            required
          />
          <small className="form-note">Note: Your email will be partially masked in the watermark</small>
        </div>
        
        {/* Optional Fields */}
        <div className="form-group">
          <label htmlFor="description">Description (Optional):</label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Tell us about your hobby item"
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="city">City (Optional):</label>
          <input
            type="text"
            id="city"
            value={city}
            onChange={(e) => setCity(e.target.value)}
            placeholder="Your city"
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="instagram">Instagram (Optional):</label>
          <input
            type="url"
            id="instagram"
            value={instagram}
            onChange={(e) => setInstagram(e.target.value)}
            placeholder="https://instagram.com/yourhandle"
            onFocus={(e) => {
              if (!e.target.value) {
                setInstagram('https://instagram.com/');
              }
            }}
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="website">Website (Optional):</label>
          <input
            type="url"
            id="website"
            value={website}
            onChange={(e) => setWebsite(e.target.value)}
            placeholder="https://yourwebsite.com"
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="image">Image: *</label>
          <div className="file-input-container">
            <div className="upload-buttons">
              <input
                type="file"
                id="camera"
                accept="image/*"
                onChange={handleFileChange}
                className="file-input mobile-camera"
                capture="environment"
              />
              <label htmlFor="camera" className="camera-input-label mobile-primary">
                📷 Take Photo
              </label>
              
              <input
                type="file"
                id="gallery"
                accept="image/*"
                onChange={handleFileChange}
                className="file-input desktop-gallery"
                required
              />
              <label htmlFor="gallery" className="file-input-label desktop-primary">
                📁 Choose from Gallery
              </label>
            </div>
            <span className="file-name">
              {file ? file.name : 'No file selected'}
            </span>
          </div>
          <small className="form-note">
            Note: All images are analyzed by AI to ensure they're appropriate hobby items.
          </small>
          
          {preview && (
            <div className="image-preview-container">
              <img src={preview} alt="Preview" className="image-preview" />
            </div>
          )}
        </div>
        
        <button type="submit" disabled={loading} className="upload-button">
          {loading ? 'Uploading...' : 'Share Your Creation'}
        </button>
      </form>
      
      {showPaymentModal && paymentData && (
        <CreatorPaymentModal
          isOpen={showPaymentModal}
          onClose={handlePaymentCancel}
          paymentData={paymentData}
          onSuccess={handlePaymentSuccess}
        />
      )}
    </div>
  );
}

export default ItemUpload;