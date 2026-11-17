import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, money, fmtDate, fmtDateTime, fmtTime } from '../api.js'

// Booking confirmation + e-ticket. "Print / Save as PDF" uses the browser's print dialog.
export default function Ticket() {
  const { id } = useParams()
  const [b, setB] = useState(null)
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  const load = () => api(`/api/bookings/${id}`).then(setB).catch((e) => setError(e.message))
  useEffect(() => { load() }, [id])

  const cancel = async () => {
    if (!window.confirm('Cancel this booking? The amount will be refunded.')) return
    setError(''); setMsg('')
    try {
      const r = await api(`/api/bookings/${id}/cancel`, { method: 'POST' })
      setB(r)
      setMsg('Booking cancelled. Refund initiated.')
    } catch (e) { setError(e.message) }
  }

  if (!b) return error ? <p className="error">{error}</p> : <p>Loading...</p>
  const confirmed = b.status === 'CONFIRMED'

  return (
    <>
      {confirmed && <div className="success no-print">🎉 Booking confirmed! Your PNR is <strong>{b.pnr}</strong></div>}
      {msg && <div className="success no-print">{msg}</div>}
      {error && <p className="error no-print">{error}</p>}

      <div className={'card ticket' + (b.status === 'CANCELLED' ? ' cancelled' : '')}>
        <div className="ticket-head">
          <div><div className="muted small">E-TICKET</div><div className="airline">{b.airlineName} · {b.flightNumber}</div></div>
          <div className="right"><div className="muted small">PNR</div><div className="pnr">{b.pnr || '—'}</div></div>
        </div>
        <div className="route big">
          <div><strong>{b.originCode}</strong><div>{b.originCity}</div><div>{fmtTime(b.departureTime)}</div></div>
          <div className="line">→</div>
          <div><strong>{b.destinationCode}</strong><div>{b.destinationCity}</div><div>{fmtTime(b.arrivalTime)}</div></div>
        </div>
        <div className="muted">{fmtDate(b.departureTime)}</div>
        <table className="table">
          <thead><tr><th>Passenger</th><th>Age</th><th>Gender</th><th>Seat</th></tr></thead>
          <tbody>{b.passengers.map((p, i) => (
            <tr key={i}><td>{p.fullName}</td><td>{p.age}</td><td>{p.gender}</td><td>{p.seatNumber}</td></tr>
          ))}</tbody>
        </table>
        <div className="grid-3 small">
          <div><span className="muted">Status</span><div><span className={'badge ' + b.status}>{b.status}</span></div></div>
          <div><span className="muted">Ticket no.</span><div>{b.ticketNumber || '—'}</div></div>
          <div><span className="muted">Amount</span><div>{money(b.totalAmount)}</div></div>
          <div><span className="muted">Payment</span><div>{b.paymentStatus || '—'} {b.paymentMethod ? '(' + b.paymentMethod + ')' : ''}</div></div>
          <div><span className="muted">Transaction</span><div>{b.transactionRef || '—'}</div></div>
          <div><span className="muted">Booked on</span><div>{fmtDateTime(b.createdAt)}</div></div>
        </div>
      </div>

      <div className="actions no-print">
        {confirmed && <button className="btn" onClick={() => window.print()}>Print / Save as PDF</button>}
        {confirmed && <button className="btn btn-danger" onClick={cancel}>Cancel booking</button>}
        <Link className="btn btn-outline" to="/my-bookings">My bookings</Link>
      </div>
    </>
  )
}
