const configuredBaseUrl = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/v1';

export const API_BASE_URL = configuredBaseUrl.replace(/\/$/, '');
// Set this to true only when deliberately using the in-memory demo API.
export const USE_MOCK = process.env.REACT_APP_USE_MOCK === 'true';
