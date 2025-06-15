import { useState, useEffect } from 'react';

function UserEmailSetter() {
  const [email, setEmail] = useState(localStorage.getItem('userEmail') || '');
  const [showBanner, setShowBanner] = useState(!localStorage.getItem('userEmail'));

  const handleSubmit = (e) => {
    e.preventDefault();
    if (email) {
      localStorage.setItem('userEmail', email);
      setShowBanner(false);
    }
  };

  if (!showBanner) return null;

  return (
    <div className="email-setter-banner">
      <form onSubmit={handleSubmit} className="email-setter-form">
        <label>
          Set your email to manage your items:
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Enter your email"
            required
          />
        </label>
        <button type="submit">Save</button>
      </form>
    </div>
  );
}

export default UserEmailSetter;