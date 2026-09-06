import React from 'react';
import { LogOut } from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../../hooks/useAuth';
import { useToast } from '../../../hooks/useToast';
import { Avatar } from '../../common/Avatar/Avatar';
import { ROUTES } from '../../../config/routes';
import logoPurple from '../../../assets/images/logos/Secondary Logo _ Logotype _ Purple.png';
import logoWhite from '../../../assets/images/logos/Secondary Logo _ Logotype _ White .png';

export const Header = () => {
  const { currentUser, logout } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  if (!currentUser) return null;

  const isAdmin = currentUser.role === 'admin';

  const handleLogout = () => {
    logout();
    showToast('Logged out successfully.');
    navigate(ROUTES.LOGIN, { replace: true });
  };

  // Determine active tab from current URL path
  const path = location.pathname;
  const activeTab =
    path === ROUTES.ADMIN_TESTS || path.startsWith('/admin/tests')
      ? 'tests'
      : path === ROUTES.ADMIN_STUDENTS
      ? 'students'
      : 'overview';

  const handleNavTab = (tab) => {
    if (tab === 'overview') navigate(ROUTES.ADMIN_DASHBOARD);
    else if (tab === 'tests') navigate(ROUTES.ADMIN_TESTS);
    else if (tab === 'students') navigate(ROUTES.ADMIN_STUDENTS);
  };

  return (
    <nav className={`dashboard-nav${isAdmin ? ' admin-nav' : ''}`}>
      <div className="nav-container">
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <img
            src={isAdmin ? logoWhite : logoPurple}
            alt="Antarang Foundation Logo"
            style={{ height: '36px', width: 'auto', objectFit: 'contain', cursor: 'pointer' }}
            onClick={() => navigate(isAdmin ? ROUTES.ADMIN_DASHBOARD : ROUTES.STUDENT_DASHBOARD)}
          />
          {isAdmin && (
            <span
              style={{
                fontSize: '11px',
                color: 'rgba(255,255,255,0.85)',
                fontWeight: 700,
                textTransform: 'uppercase',
                letterSpacing: '1px',
                background: 'rgba(255,255,255,0.15)',
                padding: '3px 8px',
                borderRadius: '6px'
              }}
            >
              Admin Panel
            </span>
          )}
        </div>

        <div className="nav-right">
          {isAdmin && (
            <div className="admin-tab-nav">
              <button
                className={`admin-tab-btn${activeTab === 'overview' ? ' active' : ''}`}
                onClick={() => handleNavTab('overview')}
              >
                Overview
              </button>
              <button
                className={`admin-tab-btn${activeTab === 'tests' ? ' active' : ''}`}
                onClick={() => handleNavTab('tests')}
              >
                Tests
              </button>
              <button
                className={`admin-tab-btn${activeTab === 'students' ? ' active' : ''}`}
                onClick={() => handleNavTab('students')}
              >
                Students
              </button>
            </div>
          )}

          <div className="user-profile-summary">
            <Avatar name={currentUser.name} isAdmin={isAdmin} size="sm" />
            <div className="user-details-text">
              <div className="user-name" style={{ color: isAdmin ? 'white' : 'var(--color-dark-text)' }}>
                {currentUser.name}
              </div>
              <div className="user-role" style={{ color: isAdmin ? 'rgba(255,255,255,0.7)' : 'var(--color-text-muted)' }}>
                {isAdmin ? '🛡 Administrator' : currentUser.branch}
              </div>
            </div>
          </div>

          <button
            className="btn-logout"
            style={isAdmin ? { borderColor: 'rgba(255,255,255,0.3)', color: 'rgba(255,255,255,0.9)' } : {}}
            onClick={handleLogout}
          >
            <LogOut size={16} />
            <span>Logout</span>
          </button>
        </div>
      </div>
    </nav>
  );
};

