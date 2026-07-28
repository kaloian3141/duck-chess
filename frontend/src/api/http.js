const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const TOKEN_KEY = 'duck-chess-token';

export function getToken() 
{
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) 
{
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() 
{
  localStorage.removeItem(TOKEN_KEY);
}

export class ApiError extends Error 
{
  constructor(message, status) 
  {
    super(message);
    this.status = status;
  }
}

async function request(path, options = {}) 
{
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  const token = getToken();
  if(token) 
  {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  if (response.status === 204) return null;

  const data = await response.json().catch(() => ({}));

  if(!response.ok) 
  {
    throw new ApiError(
      data.message || `Request failed with status ${response.status}`,
      response.status
    );
  }

  return data;
}

export const http = {
  get: (path) => request(path),
  post: (path, body) => request(path, {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  put: (path, body) => request(path, {
    method: 'PUT',
    body: JSON.stringify(body)
  }),
  delete: (path) => request(path, { method: 'DELETE' }),
};