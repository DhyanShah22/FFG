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
  },

  forgotPassword: async ({ email, mobileNumber, profileType, channel } = {}) => {
    const res = await apiClient.post('/auth/forgot-password', { email, mobileNumber, profileType, channel });
    if (res?.success) return res.data ?? null;
    throw new Error(res?.message || 'Unable to send verification code.');
  },

  resendForgotPasswordOtp: async (challengeId) => {
    const res = await apiClient.post('/auth/forgot-password/resend', { challengeId });
    if (res?.success) return res.data;
    throw new Error(res?.message || 'Unable to resend verification code.');
  },

  resetPassword: async ({ challengeId, otp, newPassword }) => {
    const res = await apiClient.post('/auth/reset-password', { challengeId, otp, newPassword });
    if (res?.success) return true;
    throw new Error(res?.message || 'Unable to reset password.');
  }
};
