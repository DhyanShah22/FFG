import { apiClient } from './api';

export const userService = {
  getUsers: async (params = {}) => {
    const res = await apiClient.get('/users', { params });
    return res.data;
  },

  getUserById: async (userId) => {
    const res = await apiClient.get(`/users/${userId}`);
    return res.data;
  },

  createUser: async (userData) => {
    const res = await apiClient.post('/users', userData);
    return res.data;
  },

  assignRoles: async (userId, roleIds) => {
    const res = await apiClient.post(`/users/${userId}/roles`, { roleIds });
    return res.data;
  },

  updateUserStatus: async (userId, status) => {
    const res = await apiClient.patch(`/users/${userId}/status`, { status });
    return res.data;
  },

  updatePlatformLanguage: async (userId, languageCode) => {
    const res = await apiClient.put(`/users/${userId}/platform-language`, { languageCode });
    return res.data;
  },

  updateAssessmentLanguage: async (userId, languageCode) => {
    const res = await apiClient.put(`/users/${userId}/assessment-language`, { languageCode });
    return res.data;
  },

  getUserConsentHistory: async (userId) => {
    const res = await apiClient.get(`/users/${userId}/consents`);
    return res.data;
  },

  withdrawConsent: async (consentId) => {
    const res = await apiClient.post(`/consents/${consentId}/withdraw`, {});
    return res.data;
  }
};
