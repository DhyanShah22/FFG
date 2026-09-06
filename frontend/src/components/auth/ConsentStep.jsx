import React from 'react';
import { ShieldCheck, UserCheck, ScrollText } from 'lucide-react';

export const ConsentStep = ({
  dob = '',
  isConsented = false,
  onChangeConsent,
  guardianName = '',
  onChangeGuardianName,
  guardianContact = '',
  onChangeGuardianContact,
  guardianRelation = '',
  onChangeGuardianRelation
}) => {
  // Compute age from DOB
  const calculateAge = (dateString) => {
    if (!dateString) return 18;
    const today = new Date();
    const birthDate = new Date(dateString);
    let age = today.getFullYear() - birthDate.getFullYear();
    const m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  };

  const userAge = calculateAge(dob);
  const isMinor = userAge < 18;

  return (
    <div className="slide-up" style={{ textAlign: 'left' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
        <ShieldCheck size={22} color="var(--color-primary-green)" />
        <div>
          <h4 style={{ fontSize: '17px', fontWeight: 700, color: 'var(--color-dark-text)', margin: 0 }}>
            {isMinor ? 'Guardian Consent Agreement' : 'Self Consent & Data Agreement'}
          </h4>
          <span style={{ fontSize: '12px', color: 'var(--color-text-muted)' }}>
            Detected Age: <strong>{userAge} years old</strong> {isMinor ? '(Under 18 - Guardian Consent Required)' : '(Adult Learner)'}
          </span>
        </div>
      </div>

      {/* Minor / Guardian Form Fields */}
      {isMinor && (
        <div style={{ background: '#fcf8ed', border: '1.5px solid rgba(225,174,37,0.3)', borderRadius: '14px', padding: '18px', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
            <UserCheck size={18} color="var(--color-accent-yellow)" />
            <h5 style={{ fontSize: '15px', fontWeight: 700, color: 'var(--color-dark-text)', margin: 0 }}>
              Parent / Guardian Contact Information
            </h5>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label">Parent / Guardian Full Name <span style={{ color: 'var(--color-error)' }}>*</span></label>
              <input
                type="text"
                className="form-input"
                value={guardianName}
                onChange={e => onChangeGuardianName(e.target.value)}
                placeholder="e.g. Meena Sharma"
              />
            </div>
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label">Guardian Mobile / Contact <span style={{ color: 'var(--color-error)' }}>*</span></label>
              <input
                type="text"
                className="form-input"
                value={guardianContact}
                onChange={e => onChangeGuardianContact(e.target.value)}
                placeholder="e.g. 9876543210"
              />
            </div>
          </div>

          <div className="form-group" style={{ marginTop: '12px', marginBottom: 0 }}>
            <label className="form-label">Relationship to Student</label>
            <select
              className="form-select"
              value={guardianRelation}
              onChange={e => onChangeGuardianRelation(e.target.value)}
            >
              <option value="Mother">Mother</option>
              <option value="Father">Father</option>
              <option value="Legal Guardian">Legal Guardian</option>
              <option value="Relative / Teacher">Relative / Teacher</option>
            </select>
          </div>
        </div>
      )}

      {/* Scrollable Consent Viewer Component */}
      <div style={{ marginBottom: '20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px', fontSize: '13px', fontWeight: 600, color: 'var(--color-dark-text)' }}>
          <ScrollText size={16} color="var(--color-primary-purple)" />
          <span>Terms of Participation &amp; Privacy Policy (v1.0)</span>
        </div>

        <div
          style={{
            height: '160px',
            overflowY: 'auto',
            padding: '16px',
            backgroundColor: '#ffffff',
            border: '1.5px solid var(--color-border-light)',
            borderRadius: '12px',
            fontSize: '13px',
            color: 'var(--color-text-muted)',
            lineHeight: 1.6
          }}
        >
          <p style={{ marginBottom: '10px' }}>
            <strong>1. Purpose of Assessment:</strong> Antarang Foundation collects information to provide scientific psychometric career aptitude profiling, counseling recommendations, and skill training connections.
          </p>
          <p style={{ marginBottom: '10px' }}>
            <strong>2. Data Privacy &amp; NGO Governance:</strong> Your personal information, test scores, and career preferences will be used strictly for career guidance and educational reporting. We do not sell data to commercial third parties.
          </p>
          <p style={{ marginBottom: '10px' }}>
            <strong>3. {isMinor ? 'Parental / Guardian Consent' : 'Self Authorization'}:</strong> {isMinor ? 'By checking consent, the designated parent or legal guardian confirms approval for the student to participate in Antarang career surveys.' : 'By checking consent, you acknowledge that your responses will generate personalized career recommendations.'}
          </p>
        </div>
      </div>

      {/* Consent Checkbox */}
      <label
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          padding: '14px 18px',
          borderRadius: '12px',
          backgroundColor: isConsented ? 'rgba(80, 167, 113, 0.08)' : '#ffffff',
          border: isConsented ? '1.5px solid var(--color-primary-green)' : '1.5px solid var(--color-border-light)',
          cursor: 'pointer',
          transition: 'all 0.2s ease'
        }}
      >
        <input
          type="checkbox"
          checked={isConsented}
          onChange={e => onChangeConsent(e.target.checked)}
          style={{ width: '18px', height: '18px', accentColor: 'var(--color-primary-green)' }}
        />
        <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--color-dark-text)' }}>
          I have read and agree to the Antarang Foundation Terms &amp; Privacy Policy <span style={{ color: 'var(--color-error)' }}>*</span>
        </span>
      </label>
    </div>
  );
};
