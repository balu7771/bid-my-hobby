import { useState, useEffect } from 'react';

function UserEmailSetter() {
  const [email, setEmail] = useState('');
  const [isSet, setIsSet] = useState(false);
  const [showInput, setShowInput] = useState(false);

  useEffect(() => {
    const savedEmail = localStorage.getItem('userEmail');
    if (savedEmail) {
      setEmail(savedEmail);
      setIsSet(true);
    }
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (email.trim()) {
      localStorage.setItem('userEmail', email.trim());
      setIsSet(true);
      setShowInput(false);
    } else {
      localStorage.removeItem('userEmail');
      setIsSet(false);
      setEmail('');
    }
    window.location.reload();
  };

  const handleEdit = () => {
    setShowInput(true);
    setIsSet(false);
  };

  const maskEmail = (email) => {
    const [username, domain] = email.split('@');
    const masked = username.charAt(0) + '*'.repeat(username.length - 2) + username.charAt(username.length - 1);
    return `${masked}@${domain}`;
  };

  if (isSet && !showInput) {
    return (
      <div className="email-display">
        <span className="email-text">{maskEmail(email)}</span>
        <button className="edit-email-btn" onClick={handleEdit}>✏️</button>
      </div>
    );
  }

  return (
    <div className="email-setter">
      <form onSubmit={handleSubmit} className="email-form">
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="Enter your email"
          required
          className="email-input"
          autoComplete="email"
        />
        <button type="submit" className="email-submit">Set</button>
      </form>
    </div>
  );
}

export default UserEmailSetter;