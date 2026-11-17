import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, money, fmtDateTime } from '../api.js'

export default function MyBookings() {
  const [list, setList] = useState(null)
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  const load = () => api('/api/bookings/my').then(setList).catch((e) => setError(e.message))
  useEffect(() => { load() }, [])

  const cancel = async (b) => {
    if (!window.confirm(`Cancel booking ${b.pnr}? The amount will be refunded.`)) return
    setError(''); setMsg('')
    try {
      await api(`/api/bookings/${b.id}/cancel`, { method: 'POST' })
      setMsg(`Booking ${b.pnr} cancelled. Refund initiated.`)
      load()
    } catch (e) { setError(e.message) }
  }

  return (
    <>
      <h2>My bookings</h2>
      {msg && <div className="success">{msg}</div>}
      {error && <p className="error">{error}</p>}
      {!list && !error && <p>Loading...</p>}
      {list && list.length === 0 && <div className="card">You have no bookings yet. <Link to="/">Search flights</Link></div>}
      {list && list.map((b) => (
        <div className="card booking-row" key={b.id}>
          <div>
            <div><strong>{b.originCode} → {b.destinationCode}</strong> <span className={'badge ' + b.status}>{b.status}</span></div>
            <div className="muted small">{b.airlineName} {b.flightNumber} · {fmtDateTime(b.departureTime)}</div>
            <div className="small">PNR: <strong>{b.pnr || '—'}</strong> · {b.passengers.length} passenger(s) · {money(b.totalAmount)}</div>
          </div>
          <div className="actions">
            {b.status === 'CONFIRMED' && <Link className="btn btn-small" to={`/booking/${b.id}`}>View ticket</Link>}
            {b.status === 'CONFIRMED' && <button className="btn btn-small btn-danger" onClick={() => cancel(b)}>Cancel</button>}
            {b.status === 'PENDING' && <Link className="btn btn-small" to={`/pay/${b.id}`}>Pay now</Link>}
          </div>
        </div>
      ))}
    </>
  )
}
