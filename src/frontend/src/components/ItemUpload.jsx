import { useState } from 'react';
import { ENDPOINTS } from '../api/apiConfig';

function ItemUpload() {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [email, setEmail] = useState('');
  const [basePrice, setBasePrice] = useState('');
  const [currency, setCurrency] = useState('USD');
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

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
    
    if (!file || !title || !description || !email || !basePrice || !currency) {
      setMessage('Please fill all fields and select an image');
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
        setMessage('Item uploaded successfully!');
        setTitle('');
        setDescription('');
        setEmail('');
        setBasePrice('');
        setCurrency('USD');
        setFile(null);
        setPreview(null);
      } else {
        // Handle specific error messages for image moderation
        if (data.message && data.message.includes('inappropriate content')) {
          setMessage('Error: The image contains inappropriate content and cannot be uploaded.');
        } else if (data.message && data.message.includes('not appear to be a hobby item')) {
          setMessage('Error: The image does not appear to be a hobby item. Only hobby items can be uploaded.');
        } else {
          setMessage(`Error: ${data.message || 'Failed to upload item'}`);
        }
      }
    } catch (error) {
      setMessage(`Error: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="upload-container">
      <h2>Share Your Hobby Creation</h2>
      {message && <div className={message.includes('Error') ? 'error-message' : 'success-message'}>{message}</div>}
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="title">Title:</label>
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
          <label htmlFor="description">Description:</label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Tell us about your hobby item"
            required
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="email">Email:</label>
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
        
        <div className="form-group price-currency-group">
          <div className="price-field">
            <label htmlFor="basePrice">Base Price:</label>
            <input
              type="number"
              id="basePrice"
              value={basePrice}
              onChange={(e) => setBasePrice(e.target.value)}
              placeholder="Starting price"
              min="0.01"
              step="0.01"
              required
            />
          </div>
          
          <div className="currency-field">
            <label htmlFor="currency">Currency:</label>
            <select
              id="currency"
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              required
            >
              <option value="USD">USD ($)</option>
              <option value="GBP">GBP (£)</option>
              <option value="INR">INR (₹)</option>
            </select>
          </div>
        </div>
        
        <div className="form-group">
          <label htmlFor="image">Image:</label>
          <div className="file-input-container">
            <input
              type="file"
              id="image"
              accept="image/*"
              onChange={handleFileChange}
              className="file-input"
              capture="environment"
              required
            />
            <div className="upload-buttons">
              <label htmlFor="image" className="file-input-label">
                Choose File
              </label>
              <div className="camera-button-container">
                <label htmlFor="camera" className="camera-input-label">
                  Take Photo
                </label>
                <small className="mobile-only-note">Works on mobile devices</small>
                <input
                  type="file"
                  id="camera"
                  accept="image/*"
                  onChange={handleFileChange}
                  className="file-input"
                  capture="user"
                />
              </div>
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
    </div>
  );
}

export default ItemUpload;