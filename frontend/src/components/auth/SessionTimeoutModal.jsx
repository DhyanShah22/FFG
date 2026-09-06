import React from 'react';
import { Clock, RefreshCw } from 'lucide-react';

export const SessionTimeoutModal = ({ isOpen, onRestart }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-card slide-up" style={{ maxWidth: '420px', textAlign: 'center', padding: '32px 24px' }}>
        <div style={{ width: '56px', height: '56px', borderRadius: '50%', backgroundColor: '#fff5f5', border: '1.5px solid #feb2b2', color: 'var(--color-error)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' }}>
          <Clock size={28} />
        </div>
        <h3 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--color-dark-text)', marginBottom: '8px' }}>
          Session Expired
        </h3>
        <p style={{ fontSize: '14px', color: 'var(--color-text-muted)', lineHeight: 1.5, marginBottom: '24px' }}>
          Your signup session was inactive for over 1 hour. For security reasons, your progress has been reset.
        </p>
        <button className="btn btn-primary" onClick={onRestart} style={{ width: '100%' }}>
          <RefreshCw size={16} /> Start Registration Over
        </button>
      </div>
    </div>
  );
};
