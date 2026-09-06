import React from 'react';

export const STEP_TITLES = [
  'Select Profile',
  'Personal Information',
  'Profile Details',
  'Consent & Agreements',
  'Verification',
  'Create Security Credentials',
  'Welcome to Antarang'
];

export const ProgressStepper = ({ currentStep = 1, totalSteps = 7 }) => {
  const progressPercentage = Math.min(100, Math.max(0, (currentStep / totalSteps) * 100));

  return (
    <div style={{ marginBottom: '28px', textAlign: 'left' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
        <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--color-primary-green)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Step {currentStep} of {totalSteps}: {STEP_TITLES[currentStep - 1] || ''}
        </span>
        <span style={{ fontSize: '12px', color: 'var(--color-text-subtle)', fontWeight: 500 }}>
          {totalSteps - currentStep > 0 ? `${totalSteps - currentStep} steps remaining` : 'Complete'}
        </span>
      </div>

      <div style={{ width: '100%', height: '6px', backgroundColor: 'var(--color-border-light)', borderRadius: '9999px', overflow: 'hidden' }}>
        <div
          style={{
            width: `${progressPercentage}%`,
            height: '100%',
            backgroundColor: 'var(--color-primary-green)',
            borderRadius: '9999px',
            transition: 'width 0.35s cubic-bezier(0.16, 1, 0.3, 1)'
          }}
        ></div>
      </div>
    </div>
  );
};
