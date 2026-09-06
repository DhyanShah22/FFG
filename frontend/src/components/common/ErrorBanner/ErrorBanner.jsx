import React from 'react';
import { AlertTriangle, ArrowRight, KeyRound } from 'lucide-react';

export const ErrorBanner = ({
  title = "This account already exists.",
  message = "An account is already registered with this email or mobile number.",
  onGoToLogin,
  onForgotPassword
}) => {
  return (
    <div
      className="slide-up"
      style={{
        background: '#fff5f5',
        border: '1.5px solid #feb2b2',
        borderRadius: '14px',
        padding: '20px',
        marginBottom: '24px',
        textAlign: 'left'
      }}
    >
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px', marginBottom: '12px' }}>
        <AlertTriangle size={20} color="var(--color-error)" style={{ flexShrink: 0, marginTop: '2px' }} />
        <div>
          <h4 style={{ fontSize: '16px', fontWeight: 700, color: '#9b2c2c', margin: 0 }}>
            {title}
          </h4>
          <p style={{ fontSize: '14px', color: '#742a2a', marginTop: '4px', lineHeight: 1.5 }}>
            {message}
          </p>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '12px', marginTop: '16px', flexWrap: 'wrap' }}>
        {onGoToLogin && (
          <button
            className="btn btn-primary"
            style={{ width: 'auto', padding: '8px 16px', fontSize: '13px' }}
            onClick={onGoToLogin}
          >
            Go to Login <ArrowRight size={14} />
          </button>
        )}
        {onForgotPassword && (
          <button
            className="btn btn-secondary"
            style={{ width: 'auto', padding: '8px 16px', fontSize: '13px' }}
            onClick={onForgotPassword}
          >
            <KeyRound size={14} /> Forgot Password
          </button>
        )}
      </div>
    </div>
  );
};
