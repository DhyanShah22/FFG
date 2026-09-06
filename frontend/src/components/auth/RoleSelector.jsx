import React from 'react';
import { Compass, UserCheck, ShieldCheck, BarChart3, Check } from 'lucide-react';

export const ROLES_LIST = [
  {
    id: 'student',
    title: 'Career Explorer',
    badge: 'Student / Youth',
    description: 'Discover your interests, aptitude, and personalized vocational career options.',
    icon: Compass,
    color: 'var(--color-primary-green)'
  },
  {
    id: 'facilitator',
    title: 'Career Counsellor',
    badge: 'Counselor / NGO Coach',
    description: 'Guide students, monitor assessment progress, and provide 1-on-1 career coaching.',
    icon: UserCheck,
    color: 'var(--color-primary-purple)'
  },
  {
    id: 'admin',
    title: 'Administrator',
    badge: 'System Admin',
    description: 'Configure assessment frameworks, manage user roles, centers, and system settings.',
    icon: ShieldCheck,
    color: 'var(--color-accent-yellow)'
  },
  {
    id: 'analyst',
    title: 'Data Analyst',
    badge: 'Insights / Evaluation',
    description: 'Access aggregate data analytics, impact indicators, and cohort reporting insights.',
    icon: BarChart3,
    color: 'var(--color-accent-lime)'
  }
];

export const RoleSelector = ({ selectedRole, onSelectRole }) => {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', margin: '20px 0' }}>
      {ROLES_LIST.map(role => {
        const Icon = role.icon;
        const isSelected = selectedRole === role.id;

        return (
          <div
            key={role.id}
            onClick={() => onSelectRole(role.id)}
            style={{
              padding: '20px',
              borderRadius: '16px',
              border: isSelected ? `2px solid ${role.color}` : '1.5px solid var(--color-border-light)',
              backgroundColor: isSelected ? 'rgba(80, 167, 113, 0.04)' : '#ffffff',
              cursor: 'pointer',
              transition: 'all 0.2s ease-in-out',
              position: 'relative',
              textAlign: 'left',
              boxShadow: isSelected ? '0 8px 20px rgba(73, 68, 92, 0.08)' : 'var(--shadow-subtle)'
            }}
            onMouseEnter={e => {
              if (!isSelected) {
                e.currentTarget.style.borderColor = 'var(--color-border-hover)';
                e.currentTarget.style.transform = 'translateY(-2px)';
              }
            }}
            onMouseLeave={e => {
              if (!isSelected) {
                e.currentTarget.style.borderColor = 'var(--color-border-light)';
                e.currentTarget.style.transform = 'none';
              }
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
              <div
                style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: '12px',
                  backgroundColor: `${role.color}15`,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                <Icon size={22} color={role.color} />
              </div>
              {isSelected && (
                <div
                  style={{
                    width: '24px',
                    height: '24px',
                    borderRadius: '50%',
                    backgroundColor: role.color,
                    color: '#ffffff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                  }}
                >
                  <Check size={14} />
                </div>
              )}
            </div>

            <h4 style={{ fontSize: '17px', fontWeight: 700, color: 'var(--color-dark-text)', marginBottom: '4px' }}>
              {role.title}
            </h4>
            <span style={{ fontSize: '11px', fontWeight: 600, color: role.color, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              {role.badge}
            </span>
            <p style={{ fontSize: '13px', color: 'var(--color-text-muted)', marginTop: '8px', lineHeight: 1.5 }}>
              {role.description}
            </p>
          </div>
        );
      })}
    </div>
  );
};
