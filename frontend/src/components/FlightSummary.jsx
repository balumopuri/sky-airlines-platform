import { fmtDate, fmtTime, fmtDuration } from '../api.js'

export default function FlightSummary({ f }) {
  return (
    <div className="flight-summary">
      <div>
        <div className="airline">{f.airlineName} · {f.flightNumber}</div>
        <div className="muted">{fmtDate(f.departureTime)}</div>
      </div>
      <div className="route">
        <div><strong>{fmtTime(f.departureTime)}</strong><div>{f.originCode} · {f.originCity}</div></div>
        <div className="line">{f.durationMinutes ? fmtDuration(f.durationMinutes) : '→'}</div>
        <div><strong>{fmtTime(f.arrivalTime)}</strong><div>{f.destinationCode} · {f.destinationCity}</div></div>
      </div>
    </div>
  )
}
