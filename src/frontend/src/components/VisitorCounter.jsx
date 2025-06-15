import { useState, useEffect } from 'react';

function VisitorCounter() {
  const [visitorCount, setVisitorCount] = useState(0);
  
  useEffect(() => {
    // Get stored count from localStorage
    const storedCount = localStorage.getItem('visitorCount');
    
    if (storedCount) {
      // If we have a stored count, increment it
      const newCount = parseInt(storedCount) + 1;
      setVisitorCount(newCount);
      localStorage.setItem('visitorCount', newCount.toString());
    } else {
      // First visit, initialize with a random number to make it look established
      const initialCount = Math.floor(Math.random() * 1000) + 500;
      setVisitorCount(initialCount);
      localStorage.setItem('visitorCount', initialCount.toString());
    }
  }, []);

  return (
    <div className="visitor-counter">
      <span className="visitor-icon">👁️</span>
      <span className="visitor-count">{visitorCount.toLocaleString()}</span>
      <span className="visitor-label">visitors</span>
    </div>
  );
}

export default VisitorCounter;