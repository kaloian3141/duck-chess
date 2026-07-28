import { createContext, useContext, useEffect, useState } from 'react';
import * as authApi from '../api/auth';
import { getToken, clearToken } from '../api/http';

const AuthContext = createContext(null);

export function AuthProvider({ children }) 
{
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function bootstrap() 
    {
      if(!getToken()) 
      {
        setLoading(false);
        return;
      }
      try 
      {
        const me = await authApi.getCurrentUser();
        setUser(me);
      } 
      catch 
      {
        clearToken();
        setUser(null);
      } 
      finally 
      {
        setLoading(false);
      }
    }
    bootstrap();
  }, []);

  async function login(usernameOrEmail, password) 
  {
    await authApi.login(usernameOrEmail, password);
    const me = await authApi.getCurrentUser();
    setUser(me);
  }

  async function register(username, email, password) 
  {
    return authApi.register(username, email, password);
  }

  async function verifyEmail(email, code) 
  {
    return authApi.verifyEmail(email, code);
  }

  async function resendVerification(email) 
  {
    return authApi.resendVerification(email);
  }

  async function forgotPassword(email) {
    return authApi.forgotPassword(email);
  }

  async function resetPassword(email, code, newPassword) 
  {
    return authApi.resetPassword(email, code, newPassword);
  }

  function logout() 
  {
    authApi.logout();
    setUser(null);
  }

  const value = {
    user,
    loading,
    isAuthenticated: user !== null,
    login,
    register,
    verifyEmail,
    resendVerification,
    forgotPassword,
    resetPassword,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() 
{
  const context = useContext(AuthContext);
  if(context === null) 
  {
    throw new Error('useAuth must be used inside <AuthProvider>');
  }
  return context;
}