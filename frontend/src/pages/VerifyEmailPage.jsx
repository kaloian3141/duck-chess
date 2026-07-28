import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { ApiError } from '../api/http.js';

export function VerifyEmailPage() {
  const { verifyEmail, resendVerification } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState(location.state?.email || '');
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [info, setInfo] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [resending, setResending] = useState(false);

  async function handleSubmit(e) 
  {
    e.preventDefault();
    setError('');
    setInfo('');
    setSubmitting(true);
    try 
    {
      await verifyEmail(email, code);
      setInfo('Email verified! Redirecting to login...');
      setTimeout(() => navigate('/login'), 1200);
    }
    catch(err)
    {
      setError(err instanceof ApiError ? err.message : 'Unexpected error');
    } 
    finally 
    {
      setSubmitting(false);
    }
  }

  async function handleResend() 
  {
    if(!email) 
    {
      setError('Enter your email first');
      return;
    }
    setError('');
    setInfo('');
    setResending(true);
    try 
    {
      await resendVerification(email);
      setInfo('New code sent — check your email');
    }
    catch(err)
    {
      setError(err instanceof ApiError ? err.message : 'Unexpected error');
    } 
    finally 
    {
      setResending(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Verify email</h1>
        <p className="auth-help">
          Enter the 6-digit code we sent to your email.
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
          <label>
            Verification code
            <input
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
              required
              inputMode="numeric"
              pattern="\d{6}"
              placeholder="123456"
              autoComplete="one-time-code"
            />
          </label>
          {error && <div className="form-error">{error}</div>}
          {info && <div className="form-info">{info}</div>}
          <button type="submit" disabled={submitting}>
            {submitting ? 'Verifying...' : 'Verify'}
          </button>
        </form>
        <button
          type="button"
          onClick={handleResend}
          disabled={resending}
          className="auth-link-button"
        >
          {resending ? 'Sending...' : 'Resend code'}
        </button>
        <p className="auth-footer">
          <Link to="/login">Back to login</Link>
        </p>
      </div>
    </div>
  );
}