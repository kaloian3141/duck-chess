import { Routes, Route, Navigate } from 'react-router-dom';
import { AppLayout } from './layouts/AppLayout.jsx';
import { PrivateRoute } from './auth/PrivateRoute.jsx';
import { LoginPage } from './pages/LoginPage.jsx';
import { RegisterPage } from './pages/RegisterPage.jsx';
import { VerifyEmailPage } from './pages/VerifyEmailPage.jsx';
import { PlayPage } from './pages/PlayPage.jsx';

function App() {
  return (
    <Routes>
      {/* Public routes — no navbar, no auth needed */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />

      {/* Protected routes — inside AppLayout with navbar */}
      <Route element={
        <PrivateRoute>
          <AppLayout />
        </PrivateRoute>
      }>
        <Route path="/play" element={<PlayPage />} />
      </Route>

      {/* Default: send to /play (which redirects to /login if not authed) */}
      <Route path="/" element={<Navigate to="/play" replace />} />
      <Route path="*" element={<Navigate to="/play" replace />} />
    </Routes>
  );
}

export default App;