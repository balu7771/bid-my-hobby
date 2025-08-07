import { useState, useEffect } from 'react';

function EnhancedChatWithAI() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [currentProfile, setCurrentProfile] = useState(null);
  const [availableProfiles, setAvailableProfiles] = useState([]);

  const aiProfiles = [
    {
      id: 'hobby_expert',
      name: 'Alex the Hobby Expert',
      avatar: '🎨',
      personality: 'Expert in various hobbies, loves to share tips and techniques',
      greeting: 'Hi! I\'m Alex, your hobby expert. I love helping people discover and improve their creative skills!'
    },
    {
      id: 'collector',
      name: 'Sam the Collector',
      avatar: '🏺',
      personality: 'Passionate collector who knows about valuing and preserving items',
      greeting: 'Hello! I\'m Sam, and I\'ve been collecting for over 20 years. Let me help you with your collection!'
    },
    {
      id: 'crafter',
      name: 'Maya the Crafter',
      avatar: '✂️',
      personality: 'Creative crafter who loves DIY projects and handmade items',
      greeting: 'Hey there! I\'m Maya, and I absolutely love creating things with my hands. What are you working on?'
    },
    {
      id: 'photographer',
      name: 'Jordan the Photographer',
      avatar: '📸',
      personality: 'Professional photographer with expertise in capturing hobby items',
      greeting: 'Hi! I\'m Jordan. I specialize in photographing beautiful hobby items. Need tips on showcasing your work?'
    },
    {
      id: 'woodworker',
      name: 'Chris the Woodworker',
      avatar: '🪵',
      personality: 'Master woodworker with decades of experience',
      greeting: 'Greetings! I\'m Chris, and I\'ve been working with wood for 30 years. What can I help you build?'
    }
  ];

  useEffect(() => {
    const randomProfile = aiProfiles[Math.floor(Math.random() * aiProfiles.length)];
    setCurrentProfile(randomProfile);
    setAvailableProfiles(aiProfiles.filter(p => p.id !== randomProfile.id));
    setMessages([{
      role: 'assistant',
      content: randomProfile.greeting,
      profile: randomProfile
    }]);
  }, []);

  const switchProfile = (profile) => {
    setCurrentProfile(profile);
    setAvailableProfiles(aiProfiles.filter(p => p.id !== profile.id));
    setMessages(prev => [...prev, {
      role: 'system',
      content: `Switched to ${profile.name}`
    }, {
      role: 'assistant',
      content: profile.greeting,
      profile: profile
    }]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!input.trim()) return;
    
    const userMessage = { role: 'user', content: input };
    setMessages(prev => [...prev, userMessage]);
    
    setInput('');
    setLoading(true);
    
    try {
      const response = await fetch('https://tinder-ai-backend.herokuapp.com/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: input,
          personality: currentProfile?.personality || 'hobby expert',
          context: 'hobby and crafts platform'
        })
      });
      
      if (response.ok) {
        const data = await response.json();
        const aiResponse = {
          role: 'assistant',
          content: data.response || data.message,
          profile: currentProfile
        };
        setMessages(prev => [...prev, aiResponse]);
      } else {
        throw new Error('API not available');
      }
    } catch (error) {
      const hobbyResponses = {
        hobby_expert: [
          "That's a fascinating hobby! Have you considered exploring different techniques to enhance your skills?",
          "I'd recommend starting with quality basic tools before investing in expensive equipment.",
          "Many successful hobbyists find that consistent practice is more valuable than expensive materials.",
          "Have you thought about joining a local hobby group? Community support can be incredibly motivating!"
        ],
        collector: [
          "That piece sounds valuable! Have you researched its provenance and market value?",
          "Proper storage and preservation are crucial for maintaining your collection's value.",
          "I always recommend documenting your collection with photos and detailed descriptions.",
          "The hobby collecting market has some interesting trends right now. What type of items interest you most?"
        ],
        crafter: [
          "I love that project idea! Have you considered using recycled materials for a unique twist?",
          "Handmade items have such character. What techniques are you most excited to try?",
          "The best crafting advice I can give is to embrace imperfections - they make each piece unique!",
          "Have you thought about selling your creations? Bid My Hobby is perfect for showcasing handmade items!"
        ],
        photographer: [
          "Great question about photography! Lighting is absolutely crucial for capturing hobby items.",
          "I recommend using natural light when possible, and a simple backdrop can make items really pop.",
          "For close-up shots of detailed work, a macro lens or macro mode can reveal amazing details.",
          "The key to great hobby photography is telling the story of the craftsmanship behind each piece."
        ],
        woodworker: [
          "Excellent choice of wood! Each species has its own character and working properties.",
          "Safety first - always wear proper protection and keep your tools sharp and well-maintained.",
          "The grain pattern in your piece will really shine with the right finish. Have you chosen a stain?",
          "Woodworking is about patience and precision. Take your time with each cut and joint."
        ]
      };
      
      const responses = hobbyResponses[currentProfile?.id] || hobbyResponses.hobby_expert;
      const aiResponse = {
        role: 'assistant',
        content: responses[Math.floor(Math.random() * responses.length)],
        profile: currentProfile
      };
      
      setMessages(prev => [...prev, aiResponse]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-header">
        <h2>Chat with AI Experts</h2>
        {currentProfile && (
          <div className="current-profile">
            <span className="profile-avatar">{currentProfile.avatar}</span>
            <span className="profile-name">{currentProfile.name}</span>
          </div>
        )}
      </div>
      
      <div className="profile-switcher">
        <p>Switch to another expert:</p>
        <div className="profile-buttons">
          {availableProfiles.map(profile => (
            <button
              key={profile.id}
              className="profile-button"
              onClick={() => switchProfile(profile)}
            >
              {profile.avatar} {profile.name}
            </button>
          ))}
        </div>
      </div>
      
      <div className="messages-container">
        {messages.map((message, index) => (
          <div 
            key={index} 
            className={`message ${message.role === 'user' ? 'user-message' : 
              message.role === 'system' ? 'system-message' : 'ai-message'}`}
          >
            {message.role === 'assistant' && message.profile && (
              <div className="message-header">
                <span className="message-avatar">{message.profile.avatar}</span>
                <span className="message-sender">{message.profile.name}</span>
              </div>
            )}
            <div className="message-bubble">
              {message.content}
            </div>
          </div>
        ))}
        
        {loading && (
          <div className="message ai-message">
            <div className="message-header">
              <span className="message-avatar">{currentProfile?.avatar}</span>
              <span className="message-sender">{currentProfile?.name}</span>
            </div>
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
          placeholder={`Ask ${currentProfile?.name || 'the AI'} anything...`}
          disabled={loading}
        />
        <button type="submit" disabled={loading || !input.trim()}>
          Send
        </button>
      </form>
    </div>
  );
}

export default EnhancedChatWithAI;