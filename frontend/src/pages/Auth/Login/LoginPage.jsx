import React, { useState } from 'react';
import { Compass, BrainCircuit, ShieldCheck, UserPlus, LogIn } from 'lucide-react';
import { LoginForm } from '../../../components/auth/LoginForm';
import { SignupWizard } from '../../../components/auth/SignupWizard';
import { ForgotPasswordFlow } from '../../../components/auth/ForgotPasswordFlow';

import logoPrimaryWhite from '../../../assets/images/logos/Signature _ Primary Logo _ White .png';
import logoPurple from '../../../assets/images/logos/Secondary Logo _ Logotype _ Purple.png';

export const LoginPage = () => {
  const [activeAuthTab, setActiveAuthTab] = useState('login'); // 'login', 'signup', 'forgot_password'

  return (
    <div className="login-screen fade-in">
      <div className="login-card" style={{ maxWidth: '1050px', borderRadius: '24px', overflow: 'hidden' }}>
        {/* Left Side: Brand Illustration & Copy */}
        <div
          className="login-left"
          style={{
            backgroundColor: 'var(--color-primary-purple)',
            padding: '48px 40px',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
            position: 'relative'
          }}
        >
          <div>
            <div className="brand-wrapper" style={{ marginBottom: '28px' }}>
              <img
                src={logoPrimaryWhite}
                alt="Antarang Foundation Logo"
                style={{ height: '52px', width: 'auto', objectFit: 'contain' }}
              />
            </div>

            <div className="login-hero-content">
              <h1 style={{ fontSize: '36px', fontWeight: 800, color: '#ffffff', lineHeight: 1.25, marginBottom: '14px' }}>
                Welcome to Antarang
              </h1>
              <p style={{ color: 'rgba(255, 255, 255, 0.85)', fontSize: '15px', lineHeight: 1.6 }}>
                Helping youth discover careers and make informed education &amp; career decisions through scientific aptitude profiling and 1-on-1 counseling.
              </p>

              <div className="login-features" style={{ marginTop: '24px', gap: '12px' }}>
                <div className="feature-item" style={{ display: 'flex', alignItems: 'center', gap: '10px', color: '#ffffff', fontSize: '13px' }}>
                  <BrainCircuit size={18} color="var(--color-accent-lime)" />
                  <span>Psychometric Aptitude &amp; Interest Mapping</span>
                </div>
                <div className="feature-item" style={{ display: 'flex', alignItems: 'center', gap: '10px', color: '#ffffff', fontSize: '13px' }}>
                  <Compass size={18} color="var(--color-accent-yellow)" />
                  <span>Personalized Vocational Career Pathways</span>
                </div>
                <div className="feature-item" style={{ display: 'flex', alignItems: 'center', gap: '10px', color: '#ffffff', fontSize: '13px' }}>
                  <ShieldCheck size={18} color="#ffffff" />
                  <span>Trusted NGO Partner &amp; Counselor Network</span>
                </div>
              </div>
            </div>
          </div>

          <div style={{ fontSize: '12px', color: 'rgba(255, 255, 255, 0.65)', marginTop: '32px' }}>
            © 2026 Antarang Foundation. Empowering youth potential across India.
          </div>
        </div>

        {/* Right Side: Auth Card Container with Tabs */}
        <div className="login-right" style={{ padding: '44px 36px', flex: 1.1 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
            <img
              src={logoPurple}
              alt="Antarang Logotype"
              style={{ height: '30px', width: 'auto' }}
            />
            {activeAuthTab !== 'forgot_password' && (
              <span style={{ fontSize: '12px', color: 'var(--color-text-subtle)', fontWeight: 600 }}>
                {activeAuthTab === 'login' ? 'Existing Member' : 'New Registration'}
              </span>
            )}
          </div>

          {/* Auth Switcher Tabs (Login vs Create Account) */}
          {activeAuthTab !== 'forgot_password' && (
            <div
              style={{
                display: 'flex',
                backgroundColor: 'var(--color-bg-light)',
                padding: '4px',
                borderRadius: '14px',
                marginBottom: '28px',
                border: '1px solid var(--color-border-light)'
              }}
            >
              <button
                type="button"
                onClick={() => setActiveAuthTab('login')}
                style={{
                  flex: 1,
                  padding: '10px 16px',
                  borderRadius: '10px',
                  border: 'none',
                  backgroundColor: activeAuthTab === 'login' ? '#ffffff' : 'transparent',
                  color: activeAuthTab === 'login' ? 'var(--color-dark-text)' : 'var(--color-text-muted)',
                  fontWeight: activeAuthTab === 'login' ? 700 : 500,
                  fontSize: '14px',
                  cursor: 'pointer',
                  boxShadow: activeAuthTab === 'login' ? '0 2px 8px rgba(73, 68, 92, 0.08)' : 'none',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '8px',
                  transition: 'all 0.2s ease'
                }}
              >
                <LogIn size={16} /> Sign In
              </button>
              <button
                type="button"
                onClick={() => setActiveAuthTab('signup')}
                style={{
                  flex: 1,
                  padding: '10px 16px',
                  borderRadius: '10px',
                  border: 'none',
                  backgroundColor: activeAuthTab === 'signup' ? '#ffffff' : 'transparent',
                  color: activeAuthTab === 'signup' ? 'var(--color-dark-text)' : 'var(--color-text-muted)',
                  fontWeight: activeAuthTab === 'signup' ? 700 : 500,
                  fontSize: '14px',
                  cursor: 'pointer',
                  boxShadow: activeAuthTab === 'signup' ? '0 2px 8px rgba(73, 68, 92, 0.08)' : 'none',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '8px',
                  transition: 'all 0.2s ease'
                }}
              >
                <UserPlus size={16} /> Create Account
              </button>
            </div>
          )}

          {/* Active Flow View */}
          {activeAuthTab === 'login' && (
            <LoginForm onForgotPassword={() => setActiveAuthTab('forgot_password')} />
          )}

          {activeAuthTab === 'signup' && (
            <SignupWizard
              onGoToLogin={() => setActiveAuthTab('login')}
              onForgotPassword={() => setActiveAuthTab('forgot_password')}
            />
          )}

          {activeAuthTab === 'forgot_password' && (
            <ForgotPasswordFlow onReturnToLogin={() => setActiveAuthTab('login')} />
          )}
        </div>
      </div>
    </div>
  );
};
