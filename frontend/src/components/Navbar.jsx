import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const isAdmin = user && user.role === 'ADMIN'

  return (
    <header className="navbar">
      <div className="nav-inner">
        <Link to="/" className="brand">✈ SkyReserve</Link>
        <nav className="nav-links">
          {!isAdmin && <NavLink to="/">Search</NavLink>}
          {user && !isAdmin && <NavLink to="/my-bookings">My Bookings</NavLink>}
          {user && !isAdmin && <NavLink to="/profile">Profile</NavLink>}
          {isAdmin && (
            <>
              <NavLink to="/admin" end>Dashboard</NavLink>
              <NavLink to="/admin/flights">Flights</NavLink>
              <NavLink to="/admin/schedules">Schedules</NavLink>
              <NavLink to="/admin/bookings">Bookings</NavLink>
              <NavLink to="/admin/payments">Payments</NavLink>
            </>
          )}
        </nav>
        <div className="nav-user">
          {user ? (
            <>
              <span className="hello">{user.fullName}{isAdmin ? ' (Admin)' : ''}</span>
              <button className="btn btn-small btn-outline" onClick={() => { logout(); navigate('/') }}>Logout</button>
            </>
          ) : (
            <>
              <Link className="btn btn-small btn-outline" to="/login">Login</Link>
              <Link className="btn btn-small" to="/register">Register</Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
