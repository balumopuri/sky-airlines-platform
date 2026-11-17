import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, todayStr } from '../api.js'

export default function Home() {
  const navigate = useNavigate()
  const [airports, setAirports] = useState([])
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [date, setDate] = useState(todayStr())
  const [passengers, setPassengers] = useState(1)
  const [error, setError] = useState('')

  useEffect(() => {
    api('/api/airports').then(setAirports).catch((e) => setError(e.message))
  }, [])

  const submit = (e) => {
    e.preventDefault()
    if (!from || !to) return setError('Please choose both airports')
    if (from === to) return setError('Departure and destination must be different')
    navigate(`/results?from=${from}&to=${to}&date=${date}&passengers=${passengers}`)
  }

  return (
    <>
      <section className="hero">
        <h1>Find your next flight</h1>
        <p>Search, choose your seat and book in minutes.</p>
      </section>
      <form className="card search-form" onSubmit={submit}>
        <label>From
          <select value={from} onChange={(e) => setFrom(e.target.value)}>
            <option value="">Select airport</option>
            {airports.map((a) => <option key={a.id} value={a.code}>{a.city} ({a.code})</option>)}
          </select>
        </label>
        <label>To
          <select value={to} onChange={(e) => setTo(e.target.value)}>
            <option value="">Select airport</option>
            {airports.map((a) => <option key={a.id} value={a.code}>{a.city} ({a.code})</option>)}
          </select>
        </label>
        <label>Date
          <input type="date" value={date} min={todayStr()} onChange={(e) => setDate(e.target.value)} required />
        </label>
        <label>Passengers
          <select value={passengers} onChange={(e) => setPassengers(Number(e.target.value))}>
            {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((n) => <option key={n} value={n}>{n}</option>)}
          </select>
        </label>
        <button className="btn" type="submit">Search flights</button>
      </form>
      {error && <p className="error">{error}</p>}
      <p className="muted center">Demo flights are available for the next 7 days.</p>
    </>
  )
}
