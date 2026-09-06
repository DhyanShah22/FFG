import React from 'react';

export const Button = ({
  variant = 'primary', // 'primary', 'secondary', 'tertiary', 'outline'
  children,
  onClick,
  type = 'button',
  disabled = false,
  className = '',
  style = {},
  ...props
}) => {
  const getVariantClass = () => {
    switch (variant) {
      case 'secondary':
        return 'btn-secondary';
      case 'tertiary':
        return 'btn-tertiary';
      case 'outline':
        return 'btn-outline';
      case 'primary':
      default:
        return 'btn-primary';
    }
  };

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`${getVariantClass()} ${className}`}
      style={style}
      {...props}
    >
      {children}
    </button>
  );
};
