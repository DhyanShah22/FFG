import React from 'react';

export const Card = ({ children, className = '', style = {}, ...props }) => {
  return (
    <div className={`brand-card ${className}`} style={style} {...props}>
      {children}
    </div>
  );
};
