import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api, money, fmtDate, fmtTime, fmtDuration } from '../api.js'

export default function Results() {
  const [params] = useSearchParams()
  const from = params.get('from')
  const to = params.get('to')
  const date = params.get('date')
  const passengers = params.get('passengers') || '1'
  const [flights, setFlights] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    setFlights(null)
    setError('')
    api(`/api/flights/search?from=${from}&to=${to}&date=${date}&passengers=${passengers}`)
      .then(setFlights)
      .catch((e) => setError(e.message))
  }, [from, to, date, passengers])

  return (
    <>
      <div className="page-head">
        <h2>{from} → {to}</h2>
        <span className="muted">{fmtDate(date)} · {passengers} passenger(s)</span>
        <Link to="/" className="btn btn-small btn-outline">Change search</Link>
      </div>
      {error && <p className="error">{error}</p>}
      {!flights && !error && <p>Searching...</p>}
      {flights && flights.length === 0 && <div className="card">No flights found for this date. Try another date (demo data covers the next 7 days).</div>}
      {flights && flights.map((f) => (
        <div className="card result" key={f.id}>
          <div>
            <div className="airline">{f.airlineName}</div>
            <div className="muted">{f.flightNumber}</div>
          </div>
          <div className="route">
            <div><strong>{fmtTime(f.departureTime)}</strong><div>{f.originCode}</div></div>
            <div className="line">{fmtDuration(f.durationMinutes)}</div>
            <div><strong>{fmtTime(f.arrivalTime)}</strong><div>{f.destinationCode}</div></div>
          </div>
          <div className="price-box">
            <div className="price">{money(f.basePrice)}</div>
            <div className="muted small">{f.availableSeats} seats left</div>
            <Link className="btn btn-small" to={`/book/${f.id}?passengers=${passengers}`}>Select</Link>
          </div>
        </div>
      ))}
    </>
  )
}
