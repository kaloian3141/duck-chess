import { Outlet } from 'react-router-dom';
import { Navbar } from '../components/Navbar.jsx';

export function AppLayout() {
  return (
    <div className="app-layout">
      <Navbar />
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  );
}