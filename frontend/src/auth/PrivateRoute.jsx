import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext.jsx';

export function PrivateRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if(loading) 
  {
    return <div className="page-loading">Loading...</div>;
  }

  if(!isAuthenticated) 
  {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}