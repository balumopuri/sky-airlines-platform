import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Results from './pages/Results.jsx'
import Book from './pages/Book.jsx'
import Pay from './pages/Pay.jsx'
import Ticket from './pages/Ticket.jsx'
import MyBookings from './pages/MyBookings.jsx'
import Profile from './pages/Profile.jsx'
import AdminDashboard from './pages/AdminDashboard.jsx'
import AdminFlights from './pages/AdminFlights.jsx'
import AdminSchedules from './pages/AdminSchedules.jsx'
import AdminBookings from './pages/AdminBookings.jsx'
import AdminPayments from './pages/AdminPayments.jsx'

export default function App() {
  return (
    <>
      <Navbar />
      <main className="container">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/results" element={<Results />} />

          <Route path="/book/:scheduleId" element={<ProtectedRoute><Book /></ProtectedRoute>} />
          <Route path="/pay/:bookingId" element={<ProtectedRoute><Pay /></ProtectedRoute>} />
          <Route path="/booking/:id" element={<ProtectedRoute><Ticket /></ProtectedRoute>} />
          <Route path="/my-bookings" element={<ProtectedRoute><MyBookings /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />

          <Route path="/admin" element={<ProtectedRoute role="ADMIN"><AdminDashboard /></ProtectedRoute>} />
          <Route path="/admin/flights" element={<ProtectedRoute role="ADMIN"><AdminFlights /></ProtectedRoute>} />
          <Route path="/admin/schedules" element={<ProtectedRoute role="ADMIN"><AdminSchedules /></ProtectedRoute>} />
          <Route path="/admin/bookings" element={<ProtectedRoute role="ADMIN"><AdminBookings /></ProtectedRoute>} />
          <Route path="/admin/payments" element={<ProtectedRoute role="ADMIN"><AdminPayments /></ProtectedRoute>} />

          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </main>
    </>
  )
}
