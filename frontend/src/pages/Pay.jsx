import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api, money } from '../api.js'
import FlightSummary from '../components/FlightSummary.jsx'

// Step 2: simulated payment. Only a SUCCESS result from the server confirms the booking.
export default function Pay() {
  const { bookingId } = useParams()
  const navigate = useNavigate()
  const [booking, setBooking] = useState(null)
  const [method, setMethod] = useState('CARD')
  const [cardNumber, setCardNumber] = useState('')
  const [upiId, setUpiId] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    api(`/api/bookings/${bookingId}`).then(setBooking).catch((e) => setError(e.message))
  }, [bookingId])

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const result = await api(`/api/bookings/${bookingId}/pay`, { method: 'POST', body: { method, cardNumber, upiId } })
      if (result.status === 'CONFIRMED') {
        navigate(`/booking/${result.id}`)
      } else {
        setBooking(result)
        setError('Payment failed. Your seats were released. Please search and book again.')
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (!booking) return error ? <p className="error">{error}</p> : <p>Loading...</p>

  if (booking.status !== 'PENDING') {
    return (
      <div className="card">
        <h2>Booking {booking.status}</h2>
        {error && <p className="error">{error}</p>}
        <button className="btn" onClick={() => navigate('/')}>Search flights again</button>
      </div>
    )
  }

  return (
    <form className="form-narrow-wide" onSubmit={submit}>
      <h2>Payment</h2>
      <div className="card"><FlightSummary f={booking} /></div>
      <div className="card">
        <h3>Passengers</h3>
        {booking.passengers.map((p, i) => (
          <div key={i} className="row-between"><span>{p.fullName} ({p.age}, {p.gender})</span><span>Seat {p.seatNumber}</span></div>
        ))}
        <hr />
        <div className="row-between"><strong>Total to pay</strong><strong className="price">{money(booking.totalAmount)}</strong></div>
        <p className="muted small">Seats are held for 10 minutes.</p>
      </div>

      <div className="card">
        <h3>Payment method (test mode)</h3>
        <div className="method-row">
          {['CARD', 'UPI', 'NETBANKING'].map((m) => (
            <label key={m} className="radio"><input type="radio" checked={method === m} onChange={() => setMethod(m)} /> {m}</label>
          ))}
        </div>
        {method === 'CARD' && (
          <label>Card number (16 digits)
            <input value={cardNumber} onChange={(e) => setCardNumber(e.target.value)} placeholder="4111 1111 1111 1111" required />
          </label>
        )}
        {method === 'UPI' && (
          <label>UPI ID<input value={upiId} onChange={(e) => setUpiId(e.target.value)} placeholder="name@bank" required /></label>
        )}
        {method === 'NETBANKING' && <p className="muted">You will be treated as paid via net banking (simulation).</p>}
        <p className="muted small">Test tip: any 16-digit card works, but a card ending in 0000 is declined.</p>
        {error && <p className="error">{error}</p>}
        <button className="btn" disabled={busy}>{busy ? 'Processing...' : `Pay ${money(booking.totalAmount)}`}</button>
      </div>
    </form>
  )
}
