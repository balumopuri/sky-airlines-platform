import { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { api, money } from '../api.js'
import SeatMap from '../components/SeatMap.jsx'
import FlightSummary from '../components/FlightSummary.jsx'

// Step 1: choose seats and enter passenger details.
export default function Book() {
  const { scheduleId } = useParams()
  const [params] = useSearchParams()
  const count = Number(params.get('passengers') || 1)
  const navigate = useNavigate()

  const [flight, setFlight] = useState(null)
  const [seats, setSeats] = useState([])
  const [selected, setSelected] = useState([])        // seat ids, in the order chosen
  const [people, setPeople] = useState({})            // seatId -> passenger details
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const load = () =>
    Promise.all([api(`/api/schedules/${scheduleId}`), api(`/api/schedules/${scheduleId}/seats`)])
      .then(([f, s]) => { setFlight(f); setSeats(s) })
      .catch((e) => setError(e.message))

  useEffect(() => { load() }, [scheduleId])

  const toggle = (seat) => {
    setError('')
    if (selected.includes(seat.id)) {
      setSelected(selected.filter((id) => id !== seat.id))
    } else if (selected.length < count) {
      setSelected([...selected, seat.id])
    } else {
      setError(`You can select only ${count} seat(s). Click a selected seat to unselect it.`)
    }
  }

  const setField = (seatId, key, value) =>
    setPeople({ ...people, [seatId]: { ...(people[seatId] || {}), [key]: value } })

  const seatById = (id) => seats.find((s) => s.id === id)
  const total = selected.reduce((sum, id) => sum + Number(seatById(id)?.price || 0), 0)

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    if (selected.length !== count) return setError(`Please select ${count} seat(s)`)
    setBusy(true)
    try {
      const body = {
        scheduleId: Number(scheduleId),
        passengers: selected.map((id) => {
          const p = people[id] || {}
          return {
            fullName: p.fullName || '',
            age: Number(p.age),
            gender: p.gender || 'Male',
            idDocumentNumber: p.idDocumentNumber || '',
            seatId: id,
          }
        }),
      }
      const booking = await api('/api/bookings', { method: 'POST', body })
      navigate(`/pay/${booking.id}`)
    } catch (err) {
      setError(err.message)
      load()              // refresh seats in case someone else took one
      setSelected([])
    } finally {
      setBusy(false)
    }
  }

  if (!flight) return error ? <p className="error">{error}</p> : <p>Loading...</p>

  return (
    <form onSubmit={submit}>
      <h2>Choose seats & passengers</h2>
      <div className="card"><FlightSummary f={flight} /></div>

      <div className="card">
        <h3>1. Select {count} seat(s)</h3>
        <SeatMap seats={seats} selected={selected} onToggle={toggle} />
      </div>

      {selected.length > 0 && (
        <div className="card">
          <h3>2. Passenger details</h3>
          {selected.map((id, i) => {
            const seat = seatById(id)
            const p = people[id] || {}
            return (
              <div className="passenger-box" key={id}>
                <strong>Passenger {i + 1} · Seat {seat.seatNumber} ({seat.seatClass}) · {money(seat.price)}</strong>
                <div className="grid-4">
                  <label>Full name<input value={p.fullName || ''} onChange={(e) => setField(id, 'fullName', e.target.value)} required /></label>
                  <label>Age<input type="number" min="0" max="120" value={p.age || ''} onChange={(e) => setField(id, 'age', e.target.value)} required /></label>
                  <label>Gender
                    <select value={p.gender || 'Male'} onChange={(e) => setField(id, 'gender', e.target.value)}>
                      <option>Male</option><option>Female</option><option>Other</option>
                    </select>
                  </label>
                  <label>ID number (optional)<input value={p.idDocumentNumber || ''} onChange={(e) => setField(id, 'idDocumentNumber', e.target.value)} /></label>
                </div>
              </div>
            )
          })}
        </div>
      )}

      {error && <p className="error">{error}</p>}
      <div className="card bar">
        <div><span className="muted">Total fare</span> <strong className="price">{money(total)}</strong></div>
        <button className="btn" disabled={busy || selected.length !== count}>{busy ? 'Please wait...' : 'Continue to payment'}</button>
      </div>
    </form>
  )
}
