import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { ROUTES } from '../config/routes';

export const ProtectedRoute = ({ children, requiredRole }) => {
  const { currentUser, isLoading } = useAuth();

  if (isLoading) {
    return null;
  }

  if (!currentUser) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  if (requiredRole && currentUser.role !== requiredRole) {
    if (currentUser.role === 'admin') {
      return <Navigate to={ROUTES.ADMIN_DASHBOARD} replace />;
    }
    return <Navigate to={ROUTES.STUDENT_DASHBOARD} replace />;
  }

  return children;
};
