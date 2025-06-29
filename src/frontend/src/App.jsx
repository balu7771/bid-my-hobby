import { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import EnhancedItemList from './components/EnhancedItemList';
import ItemUpload from './components/ItemUpload';
import UserEmailSetter from './components/UserEmailSetter';
import BidVerificationPage from './components/BidVerificationPage';
import EnhancedChatWithAI from './components/EnhancedChatWithAI';
import AboutPage from './components/AboutPage';
import VisitorCounter from './components/VisitorCounter';
import './App.css';
import './components/VerificationSuccess.css';
import './components/BidAccessModal.css';
import './components/chat.css';
import './components/about.css';
import './components/visitor-counter.css';
import './components/enhanced-features.css';
import './components/creator-payment.css';
import './components/mobile-upload.css';

function App() {
  const [activeTab, setActiveTab] = useState('browse');

  return (
    <Router>
      <div className="app-container">
        <header className="app-header">
          <div className="logo-container">
            <h1>Bid My Hobby</h1>
            <span className="tagline">Where Passion Meets Value</span>
          </div>
          <UserEmailSetter />
        </header>
        
        <Routes>
          <Route path="/verify-bids" element={<BidVerificationPage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/" element={
            <>
              <nav className="app-nav">
                <ul>
                  <li>
                    <button 
                      className={activeTab === 'browse' ? 'active' : ''} 
                      onClick={() => setActiveTab('browse')}
                    >
                      Browse Items
                    </button>
                  </li>
                  <li>
                    <button 
                      className={activeTab === 'upload' ? 'active' : ''} 
                      onClick={() => {
                        console.log('Share Your Creation clicked');
                        setActiveTab('upload');
                      }}
                    >
                      Share Your Creation
                    </button>
                  </li>
                  <li>
                    <button 
                      className={activeTab === 'chat' ? 'active' : ''} 
                      onClick={() => setActiveTab('chat')}
                    >
                      Chat With AI
                    </button>
                  </li>
                </ul>
              </nav>
              
              <main className="app-main">
                {activeTab === 'browse' && <EnhancedItemList />}
                {activeTab === 'upload' && <ItemUpload />}
                {activeTab === 'chat' && <EnhancedChatWithAI />}
              </main>
            </>
          } />
        </Routes>
        
        <footer className="app-footer">
          <p>&copy; {new Date().getFullYear()} Bid My Hobby. All rights reserved.</p>
          <Link to="/about" className="about-link">About Us</Link>
        </footer>
        
        <VisitorCounter />
      </div>
    </Router>
  );
}

export default App;