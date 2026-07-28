import { http, setToken, clearToken } from './http';

export async function register(username, email, password) 
{
  return http.post('/auth/register', { username, email, password });
}

export async function verifyEmail(email, code) 
{
  return http.post('/auth/verify-email', { email, code });
}

export async function resendVerification(email) 
{
  const params = new URLSearchParams({ email }).toString();
  return http.post(`/auth/resend-verification?${params}`);
}

export async function login(usernameOrEmail, password) 
{
  const response = await http.post('/auth/login', {
    usernameOrEmail,
    password
  });
  setToken(response.token);
  return response;
}

export function logout() 
{
  clearToken();
}

export async function getCurrentUser() 
{
  return http.get('/auth/me');
}