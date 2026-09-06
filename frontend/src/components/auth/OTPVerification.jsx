import React, { useState, useEffect, useRef } from 'react';
import { ShieldCheck, RefreshCw, CheckCircle2 } from 'lucide-react';

export const OTPVerification = ({
  destination = '', // email or phone
  mode = 'email', // 'email', 'phone', 'both'
  onVerifySuccess,
  onResendOTP
}) => {
  const [otp, setOtp] = useState(['', '', '', '']);
  const [secondsLeft, setSecondsLeft] = useState(150); // 150s total countdown
  const [isResendDisabled, setIsResendDisabled] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  const [isVerifying, setIsVerifying] = useState(false);
  const [isVerified, setIsVerified] = useState(false);
  const inputRefs = [useRef(null), useRef(null), useRef(null), useRef(null)];

  // Timer countdown
  useEffect(() => {
    if (secondsLeft <= 0) {
      setIsResendDisabled(false);
      return;
    }
    // Enable resend button after 30s (i.e. when secondsLeft <= 120)
    if (secondsLeft <= 120) {
      setIsResendDisabled(false);
    }

    const timer = setInterval(() => {
      setSecondsLeft(prev => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [secondsLeft]);

  const handleChange = (index, value) => {
    if (!/^\d*$/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value.slice(-1);
    setOtp(newOtp);
    setErrorMsg('');

    // Move cursor to next input
    if (value && index < 3) {
      inputRefs[index + 1].current.focus();
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs[index - 1].current.focus();
    }
  };

  const handleVerify = () => {
    const fullOtp = otp.join('');
    if (fullOtp.length < 4) {
      setErrorMsg('Please enter all 4 digits of the OTP.');
      return;
    }

    setIsVerifying(true);
    // Simulate API verification
    setTimeout(() => {
      setIsVerifying(false);
      if (fullOtp === '1234' || fullOtp === '9999' || fullOtp.length === 4) {
        setIsVerified(true);
        if (onVerifySuccess) onVerifySuccess(fullOtp);
      } else {
        setErrorMsg('Invalid OTP. Use demo code 1234.');
      }
    }, 600);
  };

  const handleResend = () => {
    setOtp(['', '', '', '']);
    setSecondsLeft(150);
    setIsResendDisabled(true);
    setErrorMsg('');
    if (onResendOTP) onResendOTP();
  };

  const formatTimer = (secs) => {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  if (isVerified) {
    return (
      <div className="slide-up" style={{ textAlign: 'center', padding: '24px 0' }}>
        <div style={{ width: '56px', height: '56px', borderRadius: '50%', backgroundColor: 'rgba(80, 167, 113, 0.15)', color: 'var(--color-primary-green)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' }}>
          <CheckCircle2 size={32} />
        </div>
        <h4 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--color-dark-text)' }}>Verification Complete!</h4>
        <p style={{ fontSize: '14px', color: 'var(--color-text-muted)', marginTop: '4px' }}>Your {mode === 'phone' ? 'phone number' : 'email address'} has been verified.</p>
      </div>
    );
  }

  return (
    <div className="slide-up" style={{ textAlign: 'left' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
        <ShieldCheck size={20} color="var(--color-primary-green)" />
        <h4 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--color-dark-text)', margin: 0 }}>
          Verify Security Code
        </h4>
      </div>

      <p style={{ fontSize: '14px', color: 'var(--color-text-muted)', marginBottom: '20px', lineHeight: 1.5 }}>
        Enter the 4-digit code sent to <strong>{destination || 'your registered contact'}</strong>. (Demo OTP: <strong>1234</strong>)
      </p>

      {/* 4 Digit Boxes */}
      <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-start', marginBottom: '16px' }}>
        {otp.map((digit, index) => (
          <input
            key={index}
            ref={inputRefs[index]}
            type="text"
            inputMode="numeric"
            maxLength={1}
            value={digit}
            onChange={e => handleChange(index, e.target.value)}
            onKeyDown={e => handleKeyDown(index, e)}
            style={{
              width: '56px',
              height: '56px',
              textAlign: 'center',
              fontSize: '22px',
              fontWeight: 700,
              fontFamily: 'var(--font-family)',
              color: 'var(--color-dark-text)',
              border: errorMsg ? '2px solid var(--color-error)' : '1.5px solid var(--color-border-light)',
              borderRadius: '12px',
              backgroundColor: '#ffffff',
              outline: 'none'
            }}
          />
        ))}
      </div>

      {errorMsg && (
        <div style={{ color: 'var(--color-error)', fontSize: '13px', marginBottom: '16px', fontWeight: 500 }}>
          {errorMsg}
        </div>
      )}

      {/* Controls */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '20px' }}>
        <div style={{ fontSize: '13px', color: 'var(--color-text-muted)' }}>
          {secondsLeft > 0 ? (
            <span>Code expires in <strong style={{ color: 'var(--color-primary-purple)' }}>{formatTimer(secondsLeft)}</strong></span>
          ) : (
            <span style={{ color: 'var(--color-error)' }}>Code expired. Click resend.</span>
          )}
        </div>

        <button
          type="button"
          className="btn-tertiary"
          onClick={handleResend}
          disabled={isResendDisabled}
          style={{ opacity: isResendDisabled ? 0.5 : 1, fontSize: '13px' }}
        >
          <RefreshCw size={14} /> Resend OTP
        </button>
      </div>

      <button
        type="button"
        className="btn btn-primary"
        onClick={handleVerify}
        disabled={isVerifying || otp.join('').length < 4}
        style={{ width: '100%', marginTop: '20px' }}
      >
        {isVerifying ? 'Verifying...' : 'Verify Code'}
      </button>
    </div>
  );
};
