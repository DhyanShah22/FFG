import React from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { ROUTES } from '../config/routes';
import { ROLES } from '../config/roles';

export const AuthLayout = () => {
  const { currentUser } = useAuth();

  // If already logged in, skip login page and go straight to their dashboard
  if (currentUser) {
    if (currentUser.role === ROLES.ADMIN) {
      return <Navigate to={ROUTES.ADMIN_DASHBOARD} replace />;
    }
    return <Navigate to={ROUTES.STUDENT_DASHBOARD} replace />;
  }

  return (
    <div className="app-container">
      <div className="bg-decor bg-decor-1"></div>
      <div className="bg-decor bg-decor-2"></div>
      <Outlet />
    </div>
  );
};
