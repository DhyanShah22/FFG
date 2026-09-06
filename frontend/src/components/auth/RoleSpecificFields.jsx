import React from 'react';
import { SearchableDropdown } from '../common/SearchableDropdown/SearchableDropdown';

export const RoleSpecificFields = ({ role = 'student', formData, onChangeField }) => {
  if (role === 'student') {
    const isDropout = formData.educationStage === 'Dropout';

    return (
      <div className="slide-up" style={{ textAlign: 'left' }}>
        <h4 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--color-dark-text)', marginBottom: '16px' }}>
          Career Explorer Profile
        </h4>

        {/* Education Stage */}
        <div className="form-group">
          <label className="form-label">Education Stage <span style={{ color: 'var(--color-error)' }}>*</span></label>
          <select
            className="form-select"
            value={formData.educationStage || 'In School'}
            onChange={e => onChangeField('educationStage', e.target.value)}
          >
            <option value="In School">In School</option>
            <option value="In College">In College</option>
            <option value="Dropout">Dropout</option>
            <option value="Working">Working / Apprenticeship</option>
          </select>
        </div>

        {/* Conditional Dropout Reason vs Grade */}
        {isDropout ? (
          <div className="form-group">
            <label className="form-label">Reason for Dropout <span style={{ color: 'var(--color-error)' }}>*</span></label>
            <select
              className="form-select"
              value={formData.dropoutReason || 'Financial Constraints'}
              onChange={e => onChangeField('dropoutReason', e.target.value)}
            >
              <option value="Financial Constraints">Financial Constraints</option>
              <option value="Family Support Needed">Family Support Needed</option>
              <option value="Lack of Local School Access">Lack of Local School Access</option>
              <option value="Pursuing Vocational Work">Pursuing Vocational Work</option>
              <option value="Other">Other</option>
            </select>
          </div>
        ) : (
          <div className="form-group">
            <label className="form-label">Current Grade / Year <span style={{ color: 'var(--color-error)' }}>*</span></label>
            <select
              className="form-select"
              value={formData.grade || 'Grade 10'}
              onChange={e => onChangeField('grade', e.target.value)}
            >
              <option value="Grade 8">Grade 8</option>
              <option value="Grade 9">Grade 9</option>
              <option value="Grade 10">Grade 10</option>
              <option value="Grade 11">Grade 11</option>
              <option value="Grade 12">Grade 12</option>
              <option value="Diploma / ITI">Diploma / ITI</option>
              <option value="Graduation Year 1-3">Graduation Year 1-3</option>
            </select>
          </div>
        )}

        {/* Link Profile / NGO Institution */}
        <SearchableDropdown
          label="Link Profile (NGO / Institution)"
          placeholder="Select your NGO or Center..."
          options={[
            'Antarang Foundation - Mumbai Center',
            'Antarang Foundation - Pune Center',
            'Teach For India - Partner Center',
            'Pratham Education Foundation',
            'Independent Student'
          ]}
          value={formData.ngoPartner || ''}
          onChange={val => onChangeField('ngoPartner', val)}
        />

        {/* Communication Channel */}
        <div className="form-group">
          <label className="form-label">Preferred Communication Channel</label>
          <select
            className="form-select"
            value={formData.preferredChannel || 'WhatsApp'}
            onChange={e => onChangeField('preferredChannel', e.target.value)}
          >
            <option value="WhatsApp">WhatsApp</option>
            <option value="Email">Email</option>
            <option value="SMS">SMS</option>
          </select>
        </div>

        {/* How did you hear about us? */}
        <div className="form-group">
          <label className="form-label">How did you hear about us?</label>
          <select
            className="form-select"
            value={formData.referralSource || 'School / Counselor'}
            onChange={e => onChangeField('referralSource', e.target.value)}
          >
            <option value="School / Counselor">School / Counselor</option>
            <option value="Friend / Family">Friend / Family</option>
            <option value="Social Media">Social Media</option>
            <option value="NGO Event">NGO Event / Workshop</option>
          </select>
        </div>

        {/* Optional Counsellor ID */}
        <div className="form-group">
          <label className="form-label">Career Counsellor ID (Optional)</label>
          <input
            type="text"
            className="form-input"
            value={formData.counsellorId || ''}
            onChange={e => onChangeField('counsellorId', e.target.value)}
            placeholder="e.g. CNS-8821"
          />
        </div>
      </div>
    );
  }

  if (role === 'facilitator') {
    const isNGO = formData.counsellorType !== 'Independent';

    return (
      <div className="slide-up" style={{ textAlign: 'left' }}>
        <h4 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--color-dark-text)', marginBottom: '16px' }}>
          Career Counsellor Profile
        </h4>

        {/* Counsellor Type */}
        <div className="form-group">
          <label className="form-label">Counsellor Type <span style={{ color: 'var(--color-error)' }}>*</span></label>
          <div style={{ display: 'flex', gap: '16px', marginTop: '8px' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '14px' }}>
              <input
                type="radio"
                name="counsellorType"
                value="NGO"
                checked={formData.counsellorType !== 'Independent'}
                onChange={() => onChangeField('counsellorType', 'NGO')}
              />
              <span>NGO / Institution Affiliated</span>
            </label>
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '14px' }}>
              <input
                type="radio"
                name="counsellorType"
                value="Independent"
                checked={formData.counsellorType === 'Independent'}
                onChange={() => onChangeField('counsellorType', 'Independent')}
              />
              <span>Independent Practitioner</span>
            </label>
          </div>
        </div>

        {/* Conditional Institution Fields */}
        {isNGO && (
          <SearchableDropdown
            label="Institution / NGO Name"
            required
            placeholder="Select or enter your NGO..."
            options={[
              'Antarang Foundation',
              'Magic Bus India Foundation',
              'Akanksha Foundation',
              'Yuva Unstoppable',
              'Other Institution'
            ]}
            value={formData.institutionName || ''}
            onChange={val => onChangeField('institutionName', val)}
          />
        )}

        {/* Email */}
        <div className="form-group">
          <label className="form-label">Official Work Email <span style={{ color: 'var(--color-error)' }}>*</span></label>
          <input
            type="email"
            className="form-input"
            value={formData.email || ''}
            onChange={e => onChangeField('email', e.target.value)}
            placeholder="counselor@institution.org"
          />
        </div>

        {/* Referral Source */}
        <div className="form-group">
          <label className="form-label">How did you hear about us?</label>
          <select
            className="form-select"
            value={formData.referralSource || 'NGO Network'}
            onChange={e => onChangeField('referralSource', e.target.value)}
          >
            <option value="NGO Network">NGO Network</option>
            <option value="Professional Invitation">Professional Invitation</option>
            <option value="Web Search">Web Search</option>
          </select>
        </div>
      </div>
    );
  }

  // Administrator or Data Analyst
  if (role === 'admin' || role === 'analyst') {
    const isGovt = formData.adminType === 'Government';

    return (
      <div className="slide-up" style={{ textAlign: 'left' }}>
        <h4 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--color-dark-text)', marginBottom: '16px' }}>
          {role === 'admin' ? 'Administrator' : 'Data Analyst'} Authorization Details
        </h4>

        {/* Admin Type */}
        <div className="form-group">
          <label className="form-label">Organization Category <span style={{ color: 'var(--color-error)' }}>*</span></label>
          <div style={{ display: 'flex', gap: '16px', marginTop: '8px' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '14px' }}>
              <input
                type="radio"
                name="adminType"
                value="NGO"
                checked={formData.adminType !== 'Government'}
                onChange={() => onChangeField('adminType', 'NGO')}
              />
              <span>NGO / Foundation Partner</span>
            </label>
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '14px' }}>
              <input
                type="radio"
                name="adminType"
                value="Government"
                checked={formData.adminType === 'Government'}
                onChange={() => onChangeField('adminType', 'Government')}
              />
              <span>Government Body</span>
            </label>
          </div>
        </div>

        {/* Conditional Dept vs Institution */}
        {isGovt ? (
          <div className="form-group">
            <label className="form-label">Government Department <span style={{ color: 'var(--color-error)' }}>*</span></label>
            <input
              type="text"
              className="form-input"
              value={formData.department || ''}
              onChange={e => onChangeField('department', e.target.value)}
              placeholder="e.g. Dept of Higher & Technical Education"
            />
          </div>
        ) : (
          <SearchableDropdown
            label="Institution / NGO Name"
            required
            placeholder="Select your foundation..."
            options={['Antarang Foundation HQ', 'Partner NGO Cluster', 'District Skill Development Office']}
            value={formData.institutionName || ''}
            onChange={val => onChangeField('institutionName', val)}
          />
        )}

        <div className="form-group">
          <label className="form-label">Designation <span style={{ color: 'var(--color-error)' }}>*</span></label>
          <input
            type="text"
            className="form-input"
            value={formData.designation || ''}
            onChange={e => onChangeField('designation', e.target.value)}
            placeholder="e.g. Program Manager / System Lead"
          />
        </div>

        <div className="form-group">
          <label className="form-label">Official Work Email <span style={{ color: 'var(--color-error)' }}>*</span></label>
          <input
            type="email"
            className="form-input"
            value={formData.email || ''}
            onChange={e => onChangeField('email', e.target.value)}
            placeholder="name@organization.gov.in"
          />
        </div>
      </div>
    );
  }

  return null;
};
