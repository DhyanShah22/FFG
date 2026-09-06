import { API_BASE_URL, USE_MOCK } from './config';
import { mockClient } from './api/mockClient';
import { tokenService } from './token.service';

export class ApiError extends Error {
  constructor(message, { status, code, details, path } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.details = details;
    this.path = path;
  }
}

const buildUrl = (endpoint, params) => {
  const url = new URL(`${API_BASE_URL}${endpoint}`, window.location.origin);
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return;
    (Array.isArray(value) ? value : [value]).forEach(item => url.searchParams.append(key, item));
  });
  return url.toString();
};

const request = async (endpoint, { method = 'GET', data, params, headers = {} } = {}, retry = true) => {
  const accessToken = tokenService.getAccessToken();
  const tenantId = tokenService.getTenantId();
  const requestHeaders = { Accept: 'application/json', ...headers };

  if (data !== undefined) requestHeaders['Content-Type'] = 'application/json';
  if (accessToken) requestHeaders.Authorization = `Bearer ${accessToken}`;
  if (tenantId) requestHeaders['X-Tenant-Id'] = tenantId;

  let response;
  try {
    response = await fetch(buildUrl(endpoint, params), {
      method,
      headers: requestHeaders,
      body: data === undefined ? undefined : JSON.stringify(data)
    });
  } catch (error) {
    throw new ApiError('Unable to reach the API. Confirm that the backend is running.', { cause: error });
  }

  if (response.status === 401 && retry && endpoint !== '/auth/refresh-token') {
    const refreshed = await tokenService.refresh();
    if (refreshed) return request(endpoint, { method, data, params, headers }, false);
  }

  const isJson = response.headers.get('content-type')?.includes('application/json');
  const payload = isJson ? await response.json() : null;
  if (!response.ok || payload?.success === false) {
    throw new ApiError(payload?.message || `Request failed with status ${response.status}`, {
      status: response.status,
      code: payload?.errorCode,
      details: payload?.details,
      path: payload?.path
    });
  }

  return payload;
};

const realClient = {
  get: (endpoint, config = {}) => request(endpoint, { ...config, method: 'GET' }),
  post: (endpoint, data, config = {}) => request(endpoint, { ...config, method: 'POST', data }),
  put: (endpoint, data, config = {}) => request(endpoint, { ...config, method: 'PUT', data }),
  patch: (endpoint, data, config = {}) => request(endpoint, { ...config, method: 'PATCH', data }),
  delete: (endpoint, config = {}) => request(endpoint, { ...config, method: 'DELETE' })
};

const mockApiClient = {
  get: (endpoint, config) => mockClient.get(`/api/v1${endpoint}`, config),
  post: (endpoint, data, config) => mockClient.post(`/api/v1${endpoint}`, data, config),
  put: (endpoint, data, config) => mockClient.put(`/api/v1${endpoint}`, data, config),
  patch: (endpoint, data, config) => mockClient.patch(`/api/v1${endpoint}`, data, config),
  delete: (endpoint, config) => mockClient.delete(`/api/v1${endpoint}`, config)
};

export const apiClient = USE_MOCK ? mockApiClient : realClient;
