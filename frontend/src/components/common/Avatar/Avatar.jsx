import React from 'react';
import { getInitials } from '../../../utils/formatters';

export const Avatar = ({ name = '', isAdmin = false, size = 'md' }) => {
  const initials = getInitials(name);
  const sizeMap = {
    sm: { width: '32px', height: '32px', fontSize: '12px' },
    md: { width: '40px', height: '40px', fontSize: '14px' },
    lg: { width: '48px', height: '48px', fontSize: '16px' },
  };

  const styleProps = sizeMap[size] || sizeMap.md;

  return (
    <div
      className={`avatar ${isAdmin ? 'admin-avatar' : ''}`}
      style={{
        ...styleProps,
        borderRadius: '50%',
        backgroundColor: isAdmin ? '#49445C' : 'var(--color-primary-green)',
        color: '#ffffff',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontWeight: 700
      }}
    >
      {initials}
    </div>
  );
};
