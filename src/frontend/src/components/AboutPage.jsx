import React from 'react';

function AboutPage() {
  return (
    <div className="about-container">
      <h2>About Bid My Hobby</h2>
      
      <section className="about-section">
        <h3>My Mission</h3>
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
          Bid My Hobby started as an idea I posted on social media. Slowly I got interest when the AI tools like
           Copilot and ChatGPT were release. This project moved at speed when I started using Amazon Q.
        </p>
        <p>
          More about me can be found at : Instagram ID @ Balu.reelz or Search me in LinkedIn @ Balaji Mudipalli
        </p>
      </section>
      
      <section className="about-section">
        <h3>Contact Us</h3>
        <p>
          Have questions or suggestions? We'd love to hear from you!
        </p>
        <p className="contact-info">
          Email: <a href="mailto:contact@bidmyhobby.com">bidmyhobby@gmail.com</a>
        </p>
      </section>
    </div>
  );
}

export default AboutPage;