import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export function Navbar() 
{
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() 
  {
    logout();
    navigate('/login');
  }

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/play">Duck Chess</Link>
      </div>
      <div className="navbar-menu">
        <Link to="/play">Play</Link>
        {user && (
          <>
            <span className="navbar-user">
              {user.username} ({user.currentRating})
            </span>
            <button onClick={handleLogout} className="navbar-logout">
              Log out
            </button>
          </>
        )}
      </div>
    </nav>
  );
}