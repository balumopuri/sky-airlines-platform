import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth.jsx'

// Only lets logged-in users in (and only admins if role="ADMIN").
export default function ProtectedRoute({ children, role }) {
  const { user } = useAuth()
  const location = useLocation()
  if (!user) return <Navigate to="/login" state={{ from: location.pathname + location.search }} replace />
  if (role && user.role !== role) {
    return <div className="card"><h2>Access denied</h2><p>You do not have permission to open this page.</p></div>
  }
  return children
}
