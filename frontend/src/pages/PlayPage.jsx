import { useAuth } from '../auth/AuthContext.jsx';

export function PlayPage() {
  const { user } = useAuth();

  return (
    <div className="page">
      <h1>Play</h1>
      <p>Welcome, {user.username}! Rating: {user.currentRating}</p>
      <p><em>Chess game coming soon...</em></p>
    </div>
  );
}