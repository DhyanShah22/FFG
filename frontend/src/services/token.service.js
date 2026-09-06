import { API_BASE_URL } from './config';

const ACCESS_KEY = 'cap_access_token';
const REFRESH_KEY = 'cap_refresh_token';
const TENANT_KEY = 'cap_tenant_id';

const storage = () => (typeof window === 'undefined' ? null : window.localStorage);

export const tokenService = {
  getAccessToken: () => storage()?.getItem(ACCESS_KEY) || null,
  getRefreshToken: () => storage()?.getItem(REFRESH_KEY) || null,
  getTenantId: () => storage()?.getItem(TENANT_KEY) || null,
  setTokens: (accessToken, refreshToken, tenantId) => {
    if (accessToken) storage()?.setItem(ACCESS_KEY, accessToken);
    if (refreshToken) storage()?.setItem(REFRESH_KEY, refreshToken);
    if (tenantId) storage()?.setItem(TENANT_KEY, tenantId);
  },
  clear: () => {
    storage()?.removeItem(ACCESS_KEY);
    storage()?.removeItem(REFRESH_KEY);
    storage()?.removeItem(TENANT_KEY);
  },
  refresh: async () => {
    const refreshToken = tokenService.getRefreshToken();
    if (!refreshToken) return null;

    try {
      const response = await fetch(`${API_BASE_URL}/auth/refresh-token`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify({ refreshToken })
      });
      const payload = await response.json();
      if (!response.ok || !payload?.success || !payload.data?.accessToken) throw new Error('Refresh failed');

      tokenService.setTokens(payload.data.accessToken, payload.data.refreshToken || refreshToken, payload.data.user?.tenantId);
      return payload.data.accessToken;
    } catch {
      tokenService.clear();
      return null;
    }
  }
};
