import React, { createContext, useEffect, useState } from 'react';
import { authService } from '../services/auth.service';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [loginError, setLoginError] = useState('');

  const normalizeUser = (user) => {
    if (!user) return null;
    const rawRole = user.role || user.userType || (user.roles && user.roles[0]) || '';
    // The current UI has two portals. All non-admin profile types use the
    // student portal; retain the backend value separately for future portals.
    const role = ['ADMIN', 'ADMINISTRATOR', 'SUB_ADMIN'].includes(rawRole.toUpperCase())
      ? 'admin'
      : 'student';

    return {
      ...user,
      role,
      profileRole: rawRole,
      name: user.name || `${user.firstName || ''}${user.lastName ? ` ${user.lastName}` : ''}`.trim() || user.username || ''
    };
  };

  useEffect(() => {
    const restoreSession = async () => {
      try {
        const user = await authService.getCurrentUser();
        setCurrentUser(normalizeUser(user));
      } catch {
        // An absent or expired token is an unauthenticated session, not an app error.
      } finally {
        setIsLoading(false);
      }
    };
    restoreSession();
  }, []);

  const login = async (username, password) => {
    setLoginError('');
    setIsLoading(true);
    try {
      const res = await authService.login(username, password);
      const user = normalizeUser(res.user);
      setCurrentUser(user);
      setIsLoading(false);
      return user;
    } catch (err) {
      setLoginError(err.message || 'Login failed');
      setIsLoading(false);
      throw err;
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } catch {
      // Clear client state even if the network is unavailable.
    }
    setCurrentUser(null);
  };

  return (
    <AuthContext.Provider value={{ currentUser, setCurrentUser, isLoading, loginError, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
