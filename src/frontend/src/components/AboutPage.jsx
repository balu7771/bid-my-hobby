import React from 'react';

function AboutPage() {
  return (
    <div className="about-container">
      <h2>About Bid My Hobby</h2>
      
      <section className="about-section">
        <h3>Our Mission</h3>
        <p>
          Bid My Hobby was created with a simple mission: to connect passionate hobbyists 
          and collectors with others who appreciate their craft. We provide a platform where 
          creators can showcase their work, and enthusiasts can discover unique items made 
          with care and dedication.
        </p>
      </section>
      
      <section className="about-section">
        <h3>How It Works</h3>
        <p>
          Our platform is designed to be simple and intuitive:
        </p>
        <ul className="feature-list">
          <li>
            <span className="feature-icon">🎨</span>
            <div>
              <strong>Share Your Creation</strong>
              <p>Upload photos and details about items you've created through your hobby.</p>
            </div>
          </li>
          <li>
            <span className="feature-icon">💰</span>
            <div>
              <strong>Set Your Price</strong>
              <p>Establish a base price for your creation that reflects its value.</p>
            </div>
          </li>
          <li>
            <span className="feature-icon">🔍</span>
            <div>
              <strong>Browse Items</strong>
              <p>Discover unique creations from hobbyists around the world.</p>
            </div>
          </li>
          <li>
            <span className="feature-icon">🤝</span>
            <div>
              <strong>Place Bids</strong>
              <p>Show your interest in an item by placing a bid higher than the current price.</p>
            </div>
          </li>
          <li>
            <span className="feature-icon">💬</span>
            <div>
              <strong>Chat With AI</strong>
              <p>Get assistance and answers about hobbies and using our platform.</p>
            </div>
          </li>
        </ul>
      </section>
      
      <section className="about-section">
        <h3>Our Story</h3>
        <p>
          Bid My Hobby started as a passion project by a group of hobbyists who wanted 
          to create a dedicated space for sharing their creations. What began as a small 
          community has grown into a vibrant marketplace connecting creators and collectors 
          worldwide.
        </p>
        <p>
          We believe that hobbies are more than just pastimes—they're expressions of 
          creativity, skill, and passion. Our platform celebrates the dedication that 
          goes into every handcrafted item and the joy of finding something truly unique.
        </p>
      </section>
      
      <section className="about-section">
        <h3>Contact Us</h3>
        <p>
          Have questions or suggestions? We'd love to hear from you!
        </p>
        <p className="contact-info">
          Email: <a href="mailto:contact@bidmyhobby.com">contact@bidmyhobby.com</a>
        </p>
      </section>
    </div>
  );
}

export default AboutPage;