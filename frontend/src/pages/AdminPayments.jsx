import { useEffect, useState } from 'react'
import { api, money, fmtDateTime } from '../api.js'

export default function AdminPayments() {
  const [list, setList] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { api('/api/admin/payments').then(setList).catch((e) => setError(e.message)) }, [])

  if (!list) return error ? <p className="error">{error}</p> : <p>Loading...</p>
  return (
    <>
      <h2>Payment records</h2>
      <div className="card">
        <table className="table">
          <thead><tr><th>Transaction</th><th>PNR</th><th>Customer</th><th>Method</th><th>Amount</th><th>Status</th><th>Date</th></tr></thead>
          <tbody>{list.map((p) => (
            <tr key={p.id}>
              <td>{p.transactionRef}</td><td>{p.pnr || '—'}</td><td>{p.userEmail}</td><td>{p.method}</td>
              <td>{money(p.amount)}</td><td><span className={'badge ' + p.status}>{p.status}</span></td><td>{fmtDateTime(p.createdAt)}</td>
            </tr>
          ))}</tbody>
        </table>
        {list.length === 0 && <p className="muted">No payments yet.</p>}
      </div>
    </>
  )
}
