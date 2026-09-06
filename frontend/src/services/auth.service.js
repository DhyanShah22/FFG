import { apiClient } from './api';
import { tokenService } from './token.service';

export const authService = {
  login: async (username, password) => {
    const res = await apiClient.post('/auth/login', { loginId: username, password });
    if (res?.success && res.data?.accessToken && res.data?.user) {
      tokenService.setTokens(res.data.accessToken, res.data.refreshToken, res.data.user.tenantId);
      return { success: true, user: res.data.user, token: res.data.accessToken };
    }
    throw new Error(res?.message || 'Authentication failed');
  },

  logout: async () => {
    const refreshToken = tokenService.getRefreshToken();
    try {
      await apiClient.post('/auth/logout', { refreshToken });
    } finally {
      tokenService.clear();
    }
  },

  refreshToken: async () => {
    return tokenService.refresh();
  },

  getCurrentUser: async () => {
    const res = await apiClient.get('/auth/me');
    return res.data;
  }
};
