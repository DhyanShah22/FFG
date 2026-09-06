import React from 'react';
import { Sparkles, ArrowRight, Copy } from 'lucide-react';
import { useToast } from '../../hooks/useToast';

export const SuccessScreen = ({ profileId = 'ANT-2026-894X', userName = 'Student', onContinue }) => {
  const { showToast } = useToast();

  const handleCopyId = () => {
    navigator.clipboard.writeText(profileId);
    showToast(`Copied Profile ID ${profileId} to clipboard.`);
  };

  return (
    <div className="slide-up" style={{ textAlign: 'center', padding: '32px 16px' }}>
      <div
        style={{
          width: '72px',
          height: '72px',
          borderRadius: '50%',
          backgroundColor: 'rgba(80, 167, 113, 0.15)',
          color: 'var(--color-primary-green)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 20px',
          boxShadow: '0 8px 24px rgba(80, 167, 113, 0.2)'
        }}
      >
        <Sparkles size={38} />
      </div>

      <h2 style={{ fontSize: '26px', fontWeight: 800, color: 'var(--color-dark-text)', marginBottom: '8px' }}>
        Welcome aboard, {userName}! 🎉
      </h2>

      <p style={{ fontSize: '15px', color: 'var(--color-text-muted)', maxWidth: '440px', margin: '0 auto 24px', lineHeight: 1.6 }}>
        Your profile has been created successfully. You are now ready to explore scientific career profiling and personalized guidance.
      </p>

      {/* Generated Profile ID Badge */}
      <div
        style={{
          background: '#ffffff',
          border: '1.5px solid var(--color-primary-green)',
          borderRadius: '16px',
          padding: '16px 24px',
          maxWidth: '360px',
          margin: '0 auto 28px',
          boxShadow: 'var(--shadow-subtle)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}
      >
        <div style={{ textAlign: 'left' }}>
          <span style={{ fontSize: '11px', color: 'var(--color-text-subtle)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Your Antarang Profile ID
          </span>
          <div style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-primary-purple)', letterSpacing: '1px' }}>
            {profileId}
          </div>
        </div>
        <button
          type="button"
          onClick={handleCopyId}
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            color: 'var(--color-primary-green)',
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
            fontSize: '12px',
            fontWeight: 600
          }}
        >
          <Copy size={16} /> Copy
        </button>
      </div>

      <button
        type="button"
        className="btn btn-primary"
        onClick={onContinue}
        style={{ width: '100%', maxWidth: '360px', margin: '0 auto', padding: '14px 28px' }}
      >
        Continue to Dashboard <ArrowRight size={18} />
      </button>
    </div>
  );
};
