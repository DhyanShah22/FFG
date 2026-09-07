import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, ArrowRight } from 'lucide-react';
import { RoleSelector } from './RoleSelector';
import { ProgressStepper } from './ProgressStepper';
import { SearchableDropdown } from '../common/SearchableDropdown/SearchableDropdown';
import { RoleSpecificFields } from './RoleSpecificFields';
import { ConsentStep } from './ConsentStep';
import { OTPVerification } from './OTPVerification';
import { PasswordSetup } from './PasswordSetup';
import { SuccessScreen } from './SuccessScreen';
import { SessionTimeoutModal } from './SessionTimeoutModal';
import { ErrorBanner } from '../common/ErrorBanner/ErrorBanner';
import { initialMockUsers } from '../../services/api/mockData';
import { useToast } from '../../hooks/useToast';
import { ROUTES } from '../../config/routes';

const STORAGE_KEY = 'antarang_signup_session';
const SESSION_MAX_INACTIVE_MS = 60 * 60 * 1000; // 1 hour

export const SignupWizard = ({ onGoToLogin, onForgotPassword }) => {
  const [step, setStep] = useState(1);
  const [selectedRole, setSelectedRole] = useState('student');
  const [isDuplicateUser, setIsDuplicateUser] = useState(false);
  const [isSessionExpiredModalOpen, setIsSessionExpiredModalOpen] = useState(false);
  const [isContactVerified, setIsContactVerified] = useState(false);
  const [signupSessionId, setSignupSessionId] = useState(null);

  const [formData, setFormData] = useState({
    firstName: '',
    middleName: '',
    lastName: '',
    gender: 'Female',
    dob: '2008-05-15',
    country: 'India',
    state: 'Maharashtra',
    educationStage: 'In School',
    dropoutReason: 'Financial Constraints',
    grade: 'Grade 10',
    ngoPartner: 'Antarang Foundation - Mumbai Center',
    preferredChannel: 'WhatsApp',
    referralSource: 'School / Counselor',
    counsellorId: '',
    counsellorType: 'NGO',
    institutionName: 'Antarang Foundation',
    adminType: 'NGO',
    department: '',
    designation: 'Program Lead',
    email: '',
    mobileNumber: '',
    isConsented: false,
    guardianName: '',
    guardianContact: '',
    guardianRelation: 'Mother',
    usernameOption: 'email',
    customUsername: '',
    password: '',
    confirmPassword: ''
  });

  const { showToast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    const stored = sessionStorage.getItem(STORAGE_KEY);
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        const elapsed = Date.now() - parsed.lastUpdated;
        if (elapsed > SESSION_MAX_INACTIVE_MS) {
          setIsSessionExpiredModalOpen(true);
        }
      } catch (e) {}
    }
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ lastUpdated: Date.now() }));
  }, [step]);

  const updateField = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    setIsDuplicateUser(false);

    if ((field === 'email' || field === 'mobileNumber') && isContactVerified) {
      setIsContactVerified(false);
      showToast('Contact information updated. Re-verification required.');
    }
  };

  const checkDuplicateUser = () => {
    const testEmail = formData.email.trim().toLowerCase();
    const testMobile = formData.mobileNumber.trim();

    const duplicate = initialMockUsers.find(
      u => (testEmail && u.email.toLowerCase() === testEmail) || (testMobile && u.mobileNumber === testMobile)
    );

    if (duplicate) {
      setIsDuplicateUser(true);
      return true;
    }
    setIsDuplicateUser(false);
    return false;
  };

  const createSignupSession = async () => {
    const response = await fetch('/api/v1/auth/signup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tenantCode: 'demo', profileType: 'CAREER_EXPLORER' })
    });

    const payload = await response.json().catch(() => null);
    if (!response.ok) {
      throw new Error(payload?.message || 'Failed to create signup session.');
    }

    const sessionId = payload?.data?.sessionId;
    if (!sessionId) throw new Error('Signup session id missing from backend response.');
    setSignupSessionId(sessionId);
    return sessionId;
  };

  const handleNextStep = () => {
    if (step === 2) {
      if (!formData.firstName.trim()) {
        showToast('Please enter your First Name.');
        return;
      }
      if (!formData.dob) {
        showToast('Please select your Date of Birth.');
        return;
      }
    }

    if (step === 3) {
      if (checkDuplicateUser()) {
        return;
      }
    }

    if (step === 4) {
      if (!formData.isConsented) {
        showToast('You must agree to the Terms & Privacy Policy to proceed.');
        return;
      }
    }

    if (step === 5) {
      if (!isContactVerified) {
        showToast('Please verify your OTP code to proceed.');
        return;
      }
    }

    if (step === 6) {
      if (formData.password.length < 13) {
        showToast('Password must be at least 13 characters.');
        return;
      }
      if (formData.password !== formData.confirmPassword) {
        showToast('Passwords do not match.');
        return;
      }
    }

    if (step === 1 && !signupSessionId) {
      createSignupSession()
        .then(() => setStep(prev => prev + 1))
        .catch(error => showToast(error.message || 'Failed to start signup.'));
      return;
    }

    setStep(prev => prev + 1);
  };

  const handlePrevStep = () => {
    if (step > 1) setStep(prev => prev - 1);
  };

  const handleRestartRegistration = () => {
    sessionStorage.removeItem(STORAGE_KEY);
    setIsSessionExpiredModalOpen(false);
    setStep(1);
    setIsDuplicateUser(false);
    setSignupSessionId(null);
  };

  const handleFinishSignup = async () => {
    try {
      if (!signupSessionId) {
        showToast('Signup session missing. Please restart signup.');
        return;
      }

      const response = await fetch(`/api/v1/auth/signup/sessions/${signupSessionId}/complete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });

      const payload = await response.json().catch(() => null);
      if (!response.ok) {
        throw new Error(payload?.message || 'Failed to complete signup.');
      }

      showToast('Signup completed successfully. Please log in.');
      navigate(ROUTES.LOGIN);
    } catch (error) {
      showToast(error.message || 'Failed to complete signup.');
    }
  };

  return (
    <div className="slide-up">
      <SessionTimeoutModal isOpen={isSessionExpiredModalOpen} onRestart={handleRestartRegistration} />
      {step < 7 && <ProgressStepper currentStep={step} totalSteps={7} />}
      {step > 1 && step < 7 && (
        <div style={{ textAlign: 'left', marginBottom: '16px' }}>
          <button type="button" onClick={handlePrevStep} style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: '6px', fontSize: '13px', color: 'var(--color-primary-purple)', fontWeight: 600, padding: 0 }}>
            <ArrowLeft size={16} /> Back to Step {step - 1}
          </button>
        </div>
      )}
      {isDuplicateUser && (
        <ErrorBanner
          title="Account Already Registered"
          message="An account with this email address or mobile number is already registered in our platform."
          onGoToLogin={onGoToLogin}
          onForgotPassword={onForgotPassword}
        />
      )}
      {step === 1 && (
        <div>
          <div style={{ textAlign: 'left', marginBottom: '16px' }}>
            <h3 style={{ fontSize: '22px', fontWeight: 800, color: 'var(--color-dark-text)', marginBottom: '4px' }}>
              Create Your Antarang Account
            </h3>
            <p style={{ fontSize: '14px', color: 'var(--color-text-muted)' }}>
              Select your user profile type to start personalized onboarding.
            </p>
          </div>

          <RoleSelector selectedRole={selectedRole} onSelectRole={setSelectedRole} />

          <button type="button" className="btn btn-primary" onClick={handleNextStep} style={{ width: '100%', marginTop: '12px' }}>
            Next: Personal Information <ArrowRight size={18} />
          </button>
        </div>
      )}
      {step === 2 && (
        <div className="slide-up" style={{ textAlign: 'left' }}>
          <h4 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-dark-text)', marginBottom: '16px' }}>
            Personal Details
          </h4>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '14px' }}>
            <div className="form-group">
              <label className="form-label">First Name <span style={{ color: 'var(--color-error)' }}>*</span></label>
              <input type="text" className="form-input" value={formData.firstName} onChange={e => updateField('firstName', e.target.value)} placeholder="e.g. Amit" required />
            </div>
            <div className="form-group">
              <label className="form-label">Middle Name</label>
              <input type="text" className="form-input" value={formData.middleName} onChange={e => updateField('middleName', e.target.value)} placeholder="e.g. Prakash" />
            </div>
            <div className="form-group">
              <label className="form-label">Last Name</label>
              <input type="text" className="form-input" value={formData.lastName} onChange={e => updateField('lastName', e.target.value)} placeholder="e.g. Kumar" />
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Gender <span style={{ color: 'var(--color-error)' }}>*</span></label>
              <select className="form-select" value={formData.gender} onChange={e => updateField('gender', e.target.value)}>
                <option value="Female">Female</option><option value="Male">Male</option><option value="Non-binary">Non-binary</option><option value="Prefer not to say">Prefer not to say</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Date of Birth <span style={{ color: 'var(--color-error)' }}>*</span></label>
              <input type="date" className="form-input" value={formData.dob} onChange={e => updateField('dob', e.target.value)} required />
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <SearchableDropdown label="Country" required options={['India', 'Nepal', 'Bangladesh', 'Sri Lanka']} value={formData.country} onChange={val => updateField('country', val)} />
            <SearchableDropdown label="State / Region" required options={['Maharashtra', 'Delhi', 'Karnataka', 'Tamil Nadu', 'Gujarat', 'Uttar Pradesh', 'West Bengal']} value={formData.state} onChange={val => updateField('state', val)} />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginTop: '8px' }}>
            <div className="form-group">
              <label className="form-label">Email Address <span style={{ color: 'var(--color-error)' }}>*</span></label>
              <input type="email" className="form-input" value={formData.email} onChange={e => updateField('email', e.target.value)} placeholder="name@example.com" required />
            </div>
            <div className="form-group">
              <label className="form-label">Mobile Number</label>
              <input type="text" className="form-input" value={formData.mobileNumber} onChange={e => updateField('mobileNumber', e.target.value)} placeholder="9876543210" />
            </div>
          </div>
          <button type="button" className="btn btn-primary" onClick={handleNextStep} style={{ width: '100%', marginTop: '12px' }}>
            Next: Profile Details <ArrowRight size={18} />
          </button>
        </div>
      )}
      {step === 3 && (
        <div>
          <RoleSpecificFields role={selectedRole} formData={formData} onChangeField={updateField} />
          <button type="button" className="btn btn-primary" onClick={handleNextStep} disabled={isDuplicateUser} style={{ width: '100%', marginTop: '20px' }}>
            Next: Terms &amp; Consent <ArrowRight size={18} />
          </button>
        </div>
      )}
      {step === 4 && (
        <div>
          <ConsentStep dob={formData.dob} isConsented={formData.isConsented} onChangeConsent={val => updateField('isConsented', val)} guardianName={formData.guardianName} onChangeGuardianName={val => updateField('guardianName', val)} guardianContact={formData.guardianContact} onChangeGuardianContact={val => updateField('guardianContact', val)} guardianRelation={formData.guardianRelation} onChangeGuardianRelation={val => updateField('guardianRelation', val)} />
          <button type="button" className="btn btn-primary" disabled={!formData.isConsented} onClick={handleNextStep} style={{ width: '100%', marginTop: '20px' }}>
            Next: Contact Verification <ArrowRight size={18} />
          </button>
        </div>
      )}
      {step === 5 && (
        <div>
          <OTPVerification destination={formData.email || formData.mobileNumber || '9876543210'} mode={selectedRole === 'student' ? 'both' : 'email'} onVerifySuccess={() => setIsContactVerified(true)} onResendOTP={() => showToast('New 4-digit code dispatched!')} />
          <button type="button" className="btn btn-primary" disabled={!isContactVerified} onClick={handleNextStep} style={{ width: '100%', marginTop: '20px' }}>
            Next: Create Credentials <ArrowRight size={18} />
          </button>
        </div>
      )}
      {step === 6 && (
        <div>
          <PasswordSetup role={selectedRole} verifiedEmail={formData.email || 'amit.kumar@example.com'} usernameOption={formData.usernameOption} onChangeUsernameOption={val => updateField('usernameOption', val)} customUsername={formData.customUsername} onChangeCustomUsername={val => updateField('customUsername', val)} password={formData.password} onChangePassword={val => updateField('password', val)} confirmPassword={formData.confirmPassword} onChangeConfirmPassword={val => updateField('confirmPassword', val)} autoUsername="ANT-2026-894" />
          <button type="button" className="btn btn-primary" disabled={!formData.password || !formData.confirmPassword || formData.password !== formData.confirmPassword} onClick={handleNextStep} style={{ width: '100%', marginTop: '20px' }}>
            Create Account &amp; Finish <ArrowRight size={18} />
          </button>
        </div>
      )}
      {step === 7 && (
        <SuccessScreen profileId={signupSessionId || 'ANT-2026-894X'} userName={formData.firstName || 'Learner'} onContinue={handleFinishSignup} />
      )}
    </div>
  );
};
