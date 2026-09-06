import React, { useState } from 'react';
import { User, LockKeyhole, ArrowRight, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { RoleSelector, ROLES_LIST } from './RoleSelector';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import { ROUTES } from '../../config/routes';
import { ROLES } from '../../config/roles';

export const LoginForm = ({ onForgotPassword }) => {
  const [loginStep, setLoginStep] = useState(1); // 1: Role Select, 2: Credentials Form
  const [selectedRole, setSelectedRole] = useState('student');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(true);

  const { login, isLoading, loginError } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const activeRoleConfig = ROLES_LIST.find(r => r.id === selectedRole) || ROLES_LIST[0];

  const handleRoleContinue = () => {
    setLoginStep(2);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (!username.trim()) return;

    try {
      const user = await login(username, password);
      // Navigate based on role returned by server
      if (user && user.role === ROLES.ADMIN) {
        navigate(ROUTES.ADMIN_DASHBOARD, { replace: true });
      } else {
        navigate(ROUTES.STUDENT_DASHBOARD, { replace: true });
      }
    } catch (err) {
      // loginError state in AuthContext handles inline display
    }
  };

  const handleGoogleSignIn = async () => {
    showToast('Google Sign-In integration ready. Authenticating demo account...');
    try {
      const user = await login('amit.kumar', 'student123');
      if (user && user.role === ROLES.ADMIN) {
        navigate(ROUTES.ADMIN_DASHBOARD, { replace: true });
      } else {
        navigate(ROUTES.STUDENT_DASHBOARD, { replace: true });
      }
    } catch (err) {}
  };

  return (
    <div className="slide-up" style={{ textAlign: 'left' }}>
      {/* STEP 1: Select Profile Role Cards */}
      {loginStep === 1 ? (
        <div>
          <div style={{ marginBottom: '16px' }}>
            <h3 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--color-dark-text)', marginBottom: '4px' }}>
              Select Profile Role
            </h3>
            <p style={{ fontSize: '14px', color: 'var(--color-text-muted)' }}>
              Choose your profile type to proceed to authentication.
            </p>
          </div>

          <RoleSelector selectedRole={selectedRole} onSelectRole={setSelectedRole} />

          <button
            type="button"
            className="btn btn-primary"
            onClick={handleRoleContinue}
            style={{ width: '100%', marginTop: '12px' }}
          >
            Continue as {activeRoleConfig.title} <ArrowRight size={18} />
          </button>
        </div>
      ) : (
        /* STEP 2: Credentials Form */
        <div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
            <button
              type="button"
              onClick={() => setLoginStep(1)}
              style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', color: 'var(--color-primary-purple)', fontWeight: 600 }}
            >
              <ArrowLeft size={16} /> Switch Role
            </button>
            <span style={{ fontSize: '12px', fontWeight: 700, color: activeRoleConfig.color, textTransform: 'uppercase', background: `${activeRoleConfig.color}15`, padding: '4px 10px', borderRadius: '6px' }}>
              {activeRoleConfig.badge}
            </span>
          </div>

          <h3 style={{ fontSize: '22px', fontWeight: 800, color: 'var(--color-dark-text)', marginBottom: '4px' }}>
            {activeRoleConfig.title} Sign In
          </h3>
          <p style={{ fontSize: '14px', color: 'var(--color-text-muted)', marginBottom: '24px' }}>
            {selectedRole === 'student' ? 'Enter your Username, Email, or Mobile to log in.' : 'Enter your registered work email and password.'}
          </p>

          {/* Inline Validation Error Banner */}
          {loginError && (
            <div
              className="slide-up"
              style={{
                background: '#fef2f2',
                border: '1.5px solid #fee2e2',
                color: 'var(--color-error)',
                padding: '12px 16px',
                borderRadius: '12px',
                marginBottom: '20px',
                fontSize: '13px',
                fontWeight: 500
              }}
            >
              ⚠️ {loginError}
            </div>
          )}

          <form onSubmit={handleFormSubmit}>
            {/* Username / Email field */}
            <div className="form-group">
              <label className="form-label">
                {selectedRole === 'student' ? 'Username / Email / Mobile' : 'Official Work Email'} <span style={{ color: 'var(--color-error)' }}>*</span>
              </label>
              <div className="input-wrapper">
                <span className="input-icon"><User size={18} /></span>
                <input
                  type="text"
                  className="form-input"
                  value={username}
                  onChange={e => setUsername(e.target.value)}
                  placeholder={selectedRole === 'student' ? 'e.g. amit.kumar or admin' : 'e.g. counselor@institution.org'}
                  required
                />
              </div>
            </div>

            {/* Password field */}
            <div className="form-group">
              <label className="form-label">Password / PIN <span style={{ color: 'var(--color-error)' }}>*</span></label>
              <div className="input-wrapper">
                <span className="input-icon"><LockKeyhole size={18} /></span>
                <input
                  type="password"
                  className="form-input"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                />
              </div>
            </div>

            {/* Options Row */}
            <div className="form-options" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
              <label className="checkbox-label" style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={e => setRememberMe(e.target.checked)}
                />
                <span>Remember me</span>
              </label>
              <button
                type="button"
                className="forgot-link"
                onClick={onForgotPassword}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--color-primary-purple)', fontSize: '13px', fontWeight: 600 }}
              >
                Forgot Password?
              </button>
            </div>

            {/* Submit Button */}
            <button type="submit" className="btn btn-primary" disabled={isLoading} style={{ width: '100%' }}>
              {isLoading ? 'Authenticating...' : 'Sign In'}
              {!isLoading && <ArrowRight size={18} />}
            </button>
          </form>

          {/* Google Sign In Option */}
          <div style={{ marginTop: '24px', textAlign: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', margin: '20px 0' }}>
              <div style={{ flex: 1, height: '1px', backgroundColor: 'var(--color-border-light)' }}></div>
              <span style={{ fontSize: '12px', color: 'var(--color-text-subtle)', textTransform: 'uppercase' }}>or continue with</span>
              <div style={{ flex: 1, height: '1px', backgroundColor: 'var(--color-border-light)' }}></div>
            </div>

            <button
              type="button"
              className="btn btn-outline"
              onClick={handleGoogleSignIn}
              style={{ width: '100%', justifyContent: 'center' }}
            >
              <svg width="18" height="18" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" />
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" />
              </svg>
              <span>Sign in with Google</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
