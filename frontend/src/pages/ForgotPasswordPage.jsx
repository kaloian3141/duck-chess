import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { ApiError } from '../api/http.js';

export function ForgotPasswordPage() 
{
  const { forgotPassword } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) 
  {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try{
      await forgotPassword(email);
      navigate('/reset-password', { state: { email } });
    } 
    catch (err) 
    {
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
        <h1>Forgot password</h1>
        <p className="auth-help">
          Enter your email and we'll send you a reset code.
        </p>
        <form onSubmit={handleSubmit}>
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
            />
          </label>
          {error && <div className="form-error">{error}</div>}
          <button type="submit" disabled={submitting}>
            {submitting ? 'Sending...' : 'Send reset code'}
          </button>
        </form>
        <p className="auth-footer">
          <Link to="/login">Back to login</Link>
        </p>
      </div>
    </div>
  );
}