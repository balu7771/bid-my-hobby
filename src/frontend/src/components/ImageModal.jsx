import React from 'react';

function ImageModal({ imageUrl, alt, onClose }) {
  return (
    <div className="image-modal-backdrop" onClick={onClose}>
      <div className="image-modal-content" onClick={(e) => e.stopPropagation()}>
        <button className="close-button" onClick={onClose}>×</button>
        <div className="watermarked-image-container">
          <img 
            src={imageUrl} 
            alt={alt} 
            className="full-size-image"
            onError={(e) => {
              e.target.onerror = null;
              e.target.src = 'https://via.placeholder.com/800x600?text=Image+Not+Available';
            }}
          />
          <div className="watermark">Bid My Hobby</div>
        </div>
      </div>
    </div>
  );
}

export default ImageModal;