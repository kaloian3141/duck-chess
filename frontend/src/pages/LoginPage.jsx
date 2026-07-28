import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { ApiError } from '../api/http.js';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) 
  {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try 
    {
        await login(usernameOrEmail, password);
        const from = location.state?.from?.pathname || '/play';
        navigate(from, { replace: true });
    } 
    catch(err) 
    {
        if(err instanceof ApiError && err.message === 'email not verified') 
        {
        const prefill = usernameOrEmail.includes('@') ? usernameOrEmail : '';
        navigate('/verify-email', { state: { email: prefill } });
        return;
        }
        setError(err instanceof ApiError ? err.message : 'Unexpected error');
    } 
    finally 
    {
        setSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Log in</h1>
        <form onSubmit={handleSubmit}>
          <label>
            Username or email
            <input
              type="text"
              value={usernameOrEmail}
              onChange={(e) => setUsernameOrEmail(e.target.value)}
              required
              autoComplete="username"
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
            />
          </label>
          {error && <div className="form-error">{error}</div>}
          <button type="submit" disabled={submitting}>
            {submitting ? 'Logging in...' : 'Log in'}
          </button>
        </form>
        <p className="auth-footer">
          No account? <Link to="/register">Register</Link>
        </p>
        <p className="auth-footer">
        <Link to="/forgot-password">Forgot password?</Link>
        </p>
      </div>
    </div>
  );
}