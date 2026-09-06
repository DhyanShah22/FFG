import React, { useState } from 'react';
import { KeyRound, CheckCircle2, ArrowRight } from 'lucide-react';
import { OTPVerification } from './OTPVerification';
import { PasswordSetup } from './PasswordSetup';
import { useToast } from '../../hooks/useToast';

export const ForgotPasswordFlow = ({ onReturnToLogin }) => {
  const [step, setStep] = useState(1); // 1: Contact, 2: OTP, 3: New Password, 4: Success
  const [contact, setContact] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const { showToast } = useToast();

  const handleSendOTP = (e) => {
    e.preventDefault();
    if (!contact.trim()) {
      setErrorMsg('Please enter your registered Email or Mobile number.');
      return;
    }
    setErrorMsg('');
    showToast(`Verification OTP sent to ${contact}`);
    setStep(2);
  };

  const handleOTPSuccess = () => {
    showToast('OTP verified. Set your new password.');
    setStep(3);
  };

  const handleResetPassword = (e) => {
    e.preventDefault();
    if (newPassword.length < 13) {
      setErrorMsg('Password must be at least 13 characters.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setErrorMsg('Passwords do not match.');
      return;
    }
    setErrorMsg('');
    showToast('Password reset successfully!');
    setStep(4);
  };

  return (
    <div className="slide-up" style={{ textAlign: 'left' }}>
      {/* Step 1: Enter Contact */}
      {step === 1 && (
        <form onSubmit={handleSendOTP}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
            <KeyRound size={22} color="var(--color-primary-purple)" />
            <h3 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--color-dark-text)', margin: 0 }}>
              Reset Password
            </h3>
          </div>
          <p style={{ fontSize: '14px', color: 'var(--color-text-muted)', marginBottom: '24px', lineHeight: 1.5 }}>
            Enter your registered Email address or Mobile number below to receive a 4-digit security OTP code.
          </p>

          {errorMsg && (
            <div style={{ color: 'var(--color-error)', fontSize: '13px', marginBottom: '16px' }}>
              {errorMsg}
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Email Address or Mobile Number <span style={{ color: 'var(--color-error)' }}>*</span></label>
            <input
              type="text"
              className="form-input"
              value={contact}
              onChange={e => setContact(e.target.value)}
              placeholder="e.g. amit.kumar@example.com or 9876543210"
              required
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '8px' }}>
            Send Verification OTP <ArrowRight size={16} />
          </button>
        </form>
      )}

      {/* Step 2: OTP Verification */}
      {step === 2 && (
        <OTPVerification
          destination={contact}
          mode={contact.includes('@') ? 'email' : 'phone'}
          onVerifySuccess={handleOTPSuccess}
          onResendOTP={() => showToast(`New OTP sent to ${contact}`)}
        />
      )}

      {/* Step 3: Set New Password */}
      {step === 3 && (
        <form onSubmit={handleResetPassword}>
          <PasswordSetup
            role="student"
            verifiedEmail={contact}
            password={newPassword}
            onChangePassword={setNewPassword}
            confirmPassword={confirmPassword}
            onChangeConfirmPassword={setConfirmPassword}
          />

          {errorMsg && (
            <div style={{ color: 'var(--color-error)', fontSize: '13px', marginTop: '12px' }}>
              {errorMsg}
            </div>
          )}

          <button
            type="submit"
            className="btn btn-primary"
            disabled={newPassword.length < 13 || newPassword !== confirmPassword}
            style={{ width: '100%', marginTop: '20px' }}
          >
            Save New Password
          </button>
        </form>
      )}

      {/* Step 4: Password Changed Confirmation */}
      {step === 4 && (
        <div style={{ textAlign: 'center', padding: '24px 0' }}>
          <div style={{ width: '60px', height: '60px', borderRadius: '50%', backgroundColor: 'rgba(80, 167, 113, 0.15)', color: 'var(--color-primary-green)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' }}>
            <CheckCircle2 size={36} />
          </div>
          <h3 style={{ fontSize: '22px', fontWeight: 800, color: 'var(--color-dark-text)', marginBottom: '8px' }}>
            Password Reset Complete!
          </h3>
          <p style={{ fontSize: '14px', color: 'var(--color-text-muted)', marginBottom: '24px' }}>
            Your security password has been updated. You can now sign in using your new credentials.
          </p>
          <button type="button" className="btn btn-primary" onClick={onReturnToLogin} style={{ width: '100%' }}>
            Return to Login <ArrowRight size={16} />
          </button>
        </div>
      )}
    </div>
  );
};
