import React from 'react';

export const Footer = () => {
  return (
    <footer style={{
      textAlign: 'center',
      padding: '24px',
      color: 'var(--color-text-muted)',
      fontSize: '13px',
      borderTop: '1px solid var(--color-border-light)',
      marginTop: 'auto',
      backgroundColor: '#ffffff'
    }}>
      <div className="container">
        © 2026 Antarang Foundation. Empowering youth across India through scientific career guidance.
      </div>
    </footer>
  );
};
