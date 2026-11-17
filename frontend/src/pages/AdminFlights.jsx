import { useEffect, useState } from 'react'
import { api } from '../api.js'

const emptyFlight = { flightNumber: '', airlineId: '', aircraftId: '', originId: '', destinationId: '', durationMinutes: '' }

export default function AdminFlights() {
  const [tab, setTab] = useState('flights')
  const [airports, setAirports] = useState([])
  const [airlines, setAirlines] = useState([])
  const [aircraft, setAircraft] = useState([])
  const [flights, setFlights] = useState([])
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  const [flightForm, setFlightForm] = useState(emptyFlight)
  const [editingId, setEditingId] = useState(null)
  const [airportForm, setAirportForm] = useState({ code: '', name: '', city: '', country: 'India' })
  const [airlineForm, setAirlineForm] = useState({ code: '', name: '' })
  const [aircraftForm, setAircraftForm] = useState({ model: '', totalRows: '', seatsPerRow: '' })

  const loadAll = () =>
    Promise.all([api('/api/airports'), api('/api/airlines'), api('/api/admin/aircraft'), api('/api/admin/flights')])
      .then(([ap, al, ac, fl]) => { setAirports(ap); setAirlines(al); setAircraft(ac); setFlights(fl) })
      .catch((e) => setError(e.message))
  useEffect(() => { loadAll() }, [])

  const run = async (fn, okMsg) => {
    setError(''); setMsg('')
    try { await fn(); setMsg(okMsg); loadAll() } catch (e) { setError(e.message) }
  }
  const remove = (url, label) => {
    if (window.confirm('Delete this ' + label + '?')) run(() => api(url, { method: 'DELETE' }), label + ' deleted')
  }

  const saveFlight = (e) => {
    e.preventDefault()
    const body = {
      ...flightForm,
      airlineId: Number(flightForm.airlineId), aircraftId: Number(flightForm.aircraftId),
      originId: Number(flightForm.originId), destinationId: Number(flightForm.destinationId),
      durationMinutes: Number(flightForm.durationMinutes),
    }
    run(async () => {
      if (editingId) await api('/api/admin/flights/' + editingId, { method: 'PUT', body })
      else await api('/api/admin/flights', { method: 'POST', body })
      setFlightForm(emptyFlight); setEditingId(null)
    }, editingId ? 'Flight updated' : 'Flight added')
  }

  const edit = (f) => {
    setEditingId(f.id)
    setFlightForm({ flightNumber: f.flightNumber, airlineId: f.airlineId, aircraftId: f.aircraftId, originId: f.originId, destinationId: f.destinationId, durationMinutes: f.durationMinutes })
    window.scrollTo(0, 0)
  }
  const ff = (k) => (e) => setFlightForm({ ...flightForm, [k]: e.target.value })

  return (
    <>
      <h2>Manage flights & master data</h2>
      <div className="tabs">
        {['flights', 'airports', 'airlines', 'aircraft'].map((t) => (
          <button key={t} className={'tab' + (tab === t ? ' active' : '')} onClick={() => setTab(t)}>{t[0].toUpperCase() + t.slice(1)}</button>
        ))}
      </div>
      {msg && <div className="success">{msg}</div>}
      {error && <p className="error">{error}</p>}

      {tab === 'flights' && (
        <>
          <form className="card" onSubmit={saveFlight}>
            <h3>{editingId ? 'Edit flight' : 'Add flight'}</h3>
            <div className="grid-4">
              <label>Flight number<input value={flightForm.flightNumber} onChange={ff('flightNumber')} placeholder="6E-999" required /></label>
              <label>Airline
                <select value={flightForm.airlineId} onChange={ff('airlineId')} required>
                  <option value="">Select</option>{airlines.map((a) => <option key={a.id} value={a.id}>{a.name}</option>)}
                </select></label>
              <label>Aircraft
                <select value={flightForm.aircraftId} onChange={ff('aircraftId')} required>
                  <option value="">Select</option>{aircraft.map((a) => <option key={a.id} value={a.id}>{a.model} ({a.totalRows * a.seatsPerRow} seats)</option>)}
                </select></label>
              <label>Duration (minutes)<input type="number" min="1" value={flightForm.durationMinutes} onChange={ff('durationMinutes')} required /></label>
              <label>From
                <select value={flightForm.originId} onChange={ff('originId')} required>
                  <option value="">Select</option>{airports.map((a) => <option key={a.id} value={a.id}>{a.city} ({a.code})</option>)}
                </select></label>
              <label>To
                <select value={flightForm.destinationId} onChange={ff('destinationId')} required>
                  <option value="">Select</option>{airports.map((a) => <option key={a.id} value={a.id}>{a.city} ({a.code})</option>)}
                </select></label>
            </div>
            <div className="actions">
              <button className="btn">{editingId ? 'Update flight' : 'Add flight'}</button>
              {editingId && <button type="button" className="btn btn-outline" onClick={() => { setEditingId(null); setFlightForm(emptyFlight) }}>Cancel edit</button>}
            </div>
          </form>
          <div className="card">
            <table className="table">
              <thead><tr><th>Flight</th><th>Airline</th><th>Route</th><th>Aircraft</th><th>Duration</th><th></th></tr></thead>
              <tbody>{flights.map((f) => (
                <tr key={f.id}>
                  <td>{f.flightNumber}</td><td>{f.airlineName}</td><td>{f.originCode} → {f.destinationCode}</td><td>{f.aircraftModel}</td><td>{f.durationMinutes} min</td>
                  <td className="actions"><button className="btn btn-small btn-outline" onClick={() => edit(f)}>Edit</button>
                    <button className="btn btn-small btn-danger" onClick={() => remove('/api/admin/flights/' + f.id, 'flight')}>Delete</button></td>
                </tr>
              ))}</tbody>
            </table>
          </div>
        </>
      )}

      {tab === 'airports' && (
        <>
          <form className="card" onSubmit={(e) => { e.preventDefault(); run(async () => { await api('/api/admin/airports', { method: 'POST', body: airportForm }); setAirportForm({ code: '', name: '', city: '', country: 'India' }) }, 'Airport added') }}>
            <h3>Add airport</h3>
            <div className="grid-4">
              <label>Code (3 letters)<input maxLength={3} value={airportForm.code} onChange={(e) => setAirportForm({ ...airportForm, code: e.target.value })} required /></label>
              <label>Name<input value={airportForm.name} onChange={(e) => setAirportForm({ ...airportForm, name: e.target.value })} required /></label>
              <label>City<input value={airportForm.city} onChange={(e) => setAirportForm({ ...airportForm, city: e.target.value })} required /></label>
              <label>Country<input value={airportForm.country} onChange={(e) => setAirportForm({ ...airportForm, country: e.target.value })} required /></label>
            </div>
            <button className="btn">Add airport</button>
          </form>
          <div className="card"><table className="table">
            <thead><tr><th>Code</th><th>Name</th><th>City</th><th>Country</th><th></th></tr></thead>
            <tbody>{airports.map((a) => (
              <tr key={a.id}><td>{a.code}</td><td>{a.name}</td><td>{a.city}</td><td>{a.country}</td>
                <td><button className="btn btn-small btn-danger" onClick={() => remove('/api/admin/airports/' + a.id, 'airport')}>Delete</button></td></tr>
            ))}</tbody></table></div>
        </>
      )}

      {tab === 'airlines' && (
        <>
          <form className="card" onSubmit={(e) => { e.preventDefault(); run(async () => { await api('/api/admin/airlines', { method: 'POST', body: airlineForm }); setAirlineForm({ code: '', name: '' }) }, 'Airline added') }}>
            <h3>Add airline</h3>
            <div className="grid-4">
              <label>Code (2-3 letters)<input maxLength={3} value={airlineForm.code} onChange={(e) => setAirlineForm({ ...airlineForm, code: e.target.value })} required /></label>
              <label>Name<input value={airlineForm.name} onChange={(e) => setAirlineForm({ ...airlineForm, name: e.target.value })} required /></label>
            </div>
            <button className="btn">Add airline</button>
          </form>
          <div className="card"><table className="table">
            <thead><tr><th>Code</th><th>Name</th><th></th></tr></thead>
            <tbody>{airlines.map((a) => (
              <tr key={a.id}><td>{a.code}</td><td>{a.name}</td>
                <td><button className="btn btn-small btn-danger" onClick={() => remove('/api/admin/airlines/' + a.id, 'airline')}>Delete</button></td></tr>
            ))}</tbody></table></div>
        </>
      )}

      {tab === 'aircraft' && (
        <>
          <form className="card" onSubmit={(e) => { e.preventDefault(); run(async () => { await api('/api/admin/aircraft', { method: 'POST', body: { model: aircraftForm.model, totalRows: Number(aircraftForm.totalRows), seatsPerRow: Number(aircraftForm.seatsPerRow) } }); setAircraftForm({ model: '', totalRows: '', seatsPerRow: '' }) }, 'Aircraft added') }}>
            <h3>Add aircraft</h3>
            <div className="grid-4">
              <label>Model<input value={aircraftForm.model} onChange={(e) => setAircraftForm({ ...aircraftForm, model: e.target.value })} required /></label>
              <label>Rows<input type="number" min="1" max="60" value={aircraftForm.totalRows} onChange={(e) => setAircraftForm({ ...aircraftForm, totalRows: e.target.value })} required /></label>
              <label>Seats per row (max 10)<input type="number" min="1" max="10" value={aircraftForm.seatsPerRow} onChange={(e) => setAircraftForm({ ...aircraftForm, seatsPerRow: e.target.value })} required /></label>
            </div>
            <button className="btn">Add aircraft</button>
          </form>
          <div className="card"><table className="table">
            <thead><tr><th>Model</th><th>Rows</th><th>Seats/row</th><th>Total seats</th><th></th></tr></thead>
            <tbody>{aircraft.map((a) => (
              <tr key={a.id}><td>{a.model}</td><td>{a.totalRows}</td><td>{a.seatsPerRow}</td><td>{a.totalRows * a.seatsPerRow}</td>
                <td><button className="btn btn-small btn-danger" onClick={() => remove('/api/admin/aircraft/' + a.id, 'aircraft')}>Delete</button></td></tr>
            ))}</tbody></table></div>
        </>
      )}
    </>
  )
}
