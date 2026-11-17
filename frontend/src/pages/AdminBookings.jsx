import { useEffect, useState } from 'react'
import { api, money, fmtDateTime } from '../api.js'

export default function AdminBookings() {
  const [list, setList] = useState(null)
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')
  const [filter, setFilter] = useState('ALL')

  const load = () => api('/api/admin/bookings').then(setList).catch((e) => setError(e.message))
  useEffect(() => { load() }, [])

  const cancel = async (b) => {
    if (!window.confirm(`Cancel booking ${b.pnr} and refund the customer?`)) return
    setError(''); setMsg('')
    try { await api(`/api/admin/bookings/${b.id}/cancel`, { method: 'POST' }); setMsg('Booking cancelled and refunded'); load() }
    catch (e) { setError(e.message) }
  }

  if (!list) return error ? <p className="error">{error}</p> : <p>Loading...</p>
  const shown = filter === 'ALL' ? list : list.filter((b) => b.status === filter)

  return (
    <>
      <div className="page-head">
        <h2>All bookings</h2>
        <select value={filter} onChange={(e) => setFilter(e.target.value)}>
          {['ALL', 'CONFIRMED', 'PENDING', 'CANCELLED', 'FAILED'].map((s) => <option key={s}>{s}</option>)}
        </select>
      </div>
      {msg && <div className="success">{msg}</div>}
      {error && <p className="error">{error}</p>}
      <div className="card">
        <table className="table">
          <thead><tr><th>PNR</th><th>Customer</th><th>Flight</th><th>Departure</th><th>Pax</th><th>Amount</th><th>Status</th><th></th></tr></thead>
          <tbody>{shown.map((b) => (
            <tr key={b.id}>
              <td>{b.pnr || '—'}</td><td>{b.userName}<div className="muted small">{b.userEmail}</div></td>
              <td>{b.flightNumber} {b.originCode}→{b.destinationCode}</td><td>{fmtDateTime(b.departureTime)}</td>
              <td>{b.passengers.length}</td><td>{money(b.totalAmount)}</td>
              <td><span className={'badge ' + b.status}>{b.status}</span></td>
              <td>{b.status === 'CONFIRMED' && <button className="btn btn-small btn-danger" onClick={() => cancel(b)}>Cancel</button>}</td>
            </tr>
          ))}</tbody>
        </table>
        {shown.length === 0 && <p className="muted">No bookings.</p>}
      </div>
    </>
  )
}
