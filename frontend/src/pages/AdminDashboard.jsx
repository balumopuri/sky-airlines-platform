import { useEffect, useState } from 'react'
import { api, money, fmtDateTime } from '../api.js'

export default function AdminDashboard() {
  const [s, setS] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { api('/api/admin/stats').then(setS).catch((e) => setError(e.message)) }, [])

  if (!s) return error ? <p className="error">{error}</p> : <p>Loading...</p>
  return (
    <>
      <h2>Admin dashboard</h2>
      <div className="stats">
        <div className="card stat"><div className="muted">Total flights</div><div className="num">{s.totalFlights}</div></div>
        <div className="card stat"><div className="muted">Registered users</div><div className="num">{s.totalUsers}</div></div>
        <div className="card stat"><div className="muted">Total bookings</div><div className="num">{s.totalBookings}</div></div>
        <div className="card stat"><div className="muted">Revenue (successful payments)</div><div className="num">{money(s.totalRevenue)}</div></div>
      </div>

      <div className="card">
        <h3>Upcoming flights</h3>
        <table className="table">
          <thead><tr><th>Flight</th><th>Route</th><th>Departure</th><th>Seats left</th></tr></thead>
          <tbody>{s.upcomingFlights.map((f) => (
            <tr key={f.id}><td>{f.flightNumber}</td><td>{f.originCode} → {f.destinationCode}</td><td>{fmtDateTime(f.departureTime)}</td><td>{f.availableSeats}</td></tr>
          ))}</tbody>
        </table>
      </div>

      <div className="card">
        <h3>Recent bookings</h3>
        <table className="table">
          <thead><tr><th>PNR</th><th>Customer</th><th>Route</th><th>Amount</th><th>Status</th></tr></thead>
          <tbody>{s.recentBookings.map((b) => (
            <tr key={b.id}><td>{b.pnr || '—'}</td><td>{b.userName}</td><td>{b.originCode} → {b.destinationCode}</td><td>{money(b.totalAmount)}</td><td><span className={'badge ' + b.status}>{b.status}</span></td></tr>
          ))}</tbody>
        </table>
        {s.recentBookings.length === 0 && <p className="muted">No bookings yet.</p>}
      </div>
    </>
  )
}
