import { useState } from 'react';

function ChatWithAI() {
  const [messages, setMessages] = useState([
    { 
      role: 'system', 
      content: 'Welcome to Bid My Hobby AI Assistant! Ask me anything about hobbies, crafts, or how to use this platform.' 
    }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!input.trim()) return;
    
    // Add user message to chat
    const userMessage = { role: 'user', content: input };
    setMessages(prev => [...prev, userMessage]);
    
    // Clear input and set loading
    setInput('');
    setLoading(true);
    
    try {
      // Simulate AI response (replace with actual API call in production)
      setTimeout(() => {
        const responses = [
          "That's a great hobby to pursue! Many collectors find it rewarding.",
          "I'd recommend starting with basic materials before investing in expensive equipment.",
          "Bid My Hobby is a great place to showcase your creations and connect with other enthusiasts.",
          "Photography, painting, and woodworking are among the most popular hobbies on our platform.",
          "You can upload your creations by clicking on 'Share Your Creation' in the navigation menu.",
          "Many hobbyists find that setting aside dedicated time each week helps maintain their skills.",
          "The bidding process is simple - just click on an item you're interested in and place your bid."
        ];
        
        const aiResponse = { 
          role: 'assistant', 
          content: responses[Math.floor(Math.random() * responses.length)]
        };
        
        setMessages(prev => [...prev, aiResponse]);
        setLoading(false);
      }, 1000);
    } catch (error) {
      console.error('Error getting AI response:', error);
      setMessages(prev => [...prev, { 
        role: 'assistant', 
        content: 'Sorry, I encountered an error. Please try again later.' 
      }]);
      setLoading(false);
    }
  };

  return (
    <div className="chat-container">
      <h2>Chat with Hobby AI Assistant</h2>
      <p className="chat-intro">
        Ask questions about hobbies, crafts, or how to use the Bid My Hobby platform.
      </p>
      
      <div className="messages-container">
        {messages.slice(1).map((message, index) => (
          <div 
            key={index} 
            className={`message ${message.role === 'user' ? 'user-message' : 'ai-message'}`}
          >
            <div className="message-bubble">
              {message.content}
            </div>
          </div>
        ))}
        
        {loading && (
          <div className="message ai-message">
            <div className="message-bubble typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        )}
      </div>
      
      <form onSubmit={handleSubmit} className="chat-input-form">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Type your message here..."
          disabled={loading}
        />
        <button type="submit" disabled={loading || !input.trim()}>
          Send
        </button>
      </form>
    </div>
  );
}

export default ChatWithAI;