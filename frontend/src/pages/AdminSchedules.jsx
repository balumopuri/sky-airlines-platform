import { useEffect, useState } from 'react'
import { api, money, fmtDateTime } from '../api.js'
import SeatMap from '../components/SeatMap.jsx'

export default function AdminSchedules() {
  const [schedules, setSchedules] = useState([])
  const [flights, setFlights] = useState([])
  const [form, setForm] = useState({ flightId: '', departureTime: '', basePrice: '' })
  const [seatsFor, setSeatsFor] = useState(null)   // schedule being managed
  const [seats, setSeats] = useState([])
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  const load = () =>
    Promise.all([api('/api/admin/schedules'), api('/api/admin/flights')])
      .then(([s, f]) => { setSchedules(s); setFlights(f) }).catch((e) => setError(e.message))
  useEffect(() => { load() }, [])

  const add = async (e) => {
    e.preventDefault()
    setError(''); setMsg('')
    try {
      await api('/api/admin/schedules', { method: 'POST', body: { flightId: Number(form.flightId), departureTime: form.departureTime, basePrice: Number(form.basePrice) } })
      setMsg('Schedule created with all seats')
      setForm({ flightId: '', departureTime: '', basePrice: '' })
      load()
    } catch (err) { setError(err.message) }
  }

  const cancel = async (s) => {
    if (!window.confirm('Cancel this schedule? It will no longer appear in search.')) return
    setError(''); setMsg('')
    try { await api(`/api/admin/schedules/${s.id}/cancel`, { method: 'PUT' }); load() } catch (err) { setError(err.message) }
  }

  const openSeats = async (s) => {
    setError('')
    try { setSeats(await api(`/api/admin/schedules/${s.id}/seats`)); setSeatsFor(s) } catch (err) { setError(err.message) }
  }

  const toggleBlock = async (seat) => {
    setError('')
    try {
      const updated = await api(`/api/admin/seats/${seat.id}/block?blocked=${seat.status === 'AVAILABLE'}`, { method: 'PUT' })
      setSeats(seats.map((x) => (x.id === updated.id ? updated : x)))
      load()
    } catch (err) { setError(err.message) }
  }

  return (
    <>
      <h2>Flight schedules & seat availability</h2>
      {msg && <div className="success">{msg}</div>}
      {error && <p className="error">{error}</p>}

      <form className="card" onSubmit={add}>
        <h3>Add schedule</h3>
        <div className="grid-4">
          <label>Flight
            <select value={form.flightId} onChange={(e) => setForm({ ...form, flightId: e.target.value })} required>
              <option value="">Select</option>
              {flights.map((f) => <option key={f.id} value={f.id}>{f.flightNumber} · {f.originCode}→{f.destinationCode}</option>)}
            </select></label>
          <label>Departure<input type="datetime-local" value={form.departureTime} onChange={(e) => setForm({ ...form, departureTime: e.target.value })} required /></label>
          <label>Base price (₹)<input type="number" min="1" value={form.basePrice} onChange={(e) => setForm({ ...form, basePrice: e.target.value })} required /></label>
        </div>
        <button className="btn">Create schedule</button>
      </form>

      {seatsFor && (
        <div className="card">
          <div className="row-between">
            <h3>Seats: {seatsFor.flightNumber} · {fmtDateTime(seatsFor.departureTime)}</h3>
            <button className="btn btn-small btn-outline" onClick={() => setSeatsFor(null)}>Close</button>
          </div>
          <p className="muted small">Click an available seat to block it, or a blocked seat to release it.</p>
          <SeatMap seats={seats} mode="admin" onToggle={toggleBlock} />
        </div>
      )}

      <div className="card">
        <table className="table">
          <thead><tr><th>Flight</th><th>Route</th><th>Departure</th><th>Price</th><th>Seats left</th><th>Status</th><th></th></tr></thead>
          <tbody>{schedules.map((s) => (
            <tr key={s.id}>
              <td>{s.flightNumber}</td><td>{s.originCode} → {s.destinationCode}</td><td>{fmtDateTime(s.departureTime)}</td>
              <td>{money(s.basePrice)}</td><td>{s.availableSeats}</td><td><span className={'badge ' + s.status}>{s.status}</span></td>
              <td className="actions">
                <button className="btn btn-small btn-outline" onClick={() => openSeats(s)}>Seats</button>
                {s.status === 'SCHEDULED' && <button className="btn btn-small btn-danger" onClick={() => cancel(s)}>Cancel</button>}
              </td>
            </tr>
          ))}</tbody>
        </table>
      </div>
    </>
  )
}
