// Draws the seats of one flight. rows are numbers (12), letters are A-F.
// mode "select": customer picks seats. mode "admin": admin blocks/unblocks seats.
export default function SeatMap({ seats, selected = [], onToggle, mode = 'select' }) {
  const rows = {}
  seats.forEach((s) => {
    const row = parseInt(s.seatNumber, 10)
    if (!rows[row]) rows[row] = []
    rows[row].push(s)
  })

  const className = (s) => {
    if (selected.includes(s.id)) return 'seat selected'
    if (s.status === 'AVAILABLE') return 'seat available' + (s.seatClass === 'BUSINESS' ? ' business' : '')
    if (s.status === 'BLOCKED') return 'seat blocked'
    return 'seat taken'
  }

  const clickable = (s) =>
    mode === 'admin' ? s.status === 'AVAILABLE' || s.status === 'BLOCKED' : s.status === 'AVAILABLE' || selected.includes(s.id)

  return (
    <div className="seatmap">
      {Object.keys(rows).map((r) => {
        const list = rows[r]
        const half = Math.ceil(list.length / 2)
        return (
          <div className="seat-row" key={r}>
            <span className="row-no">{r}</span>
            {list.map((s, i) => (
              <span key={s.id} style={{ display: 'contents' }}>
                {i === half && <span className="aisle" />}
                <button
                  type="button"
                  className={className(s)}
                  disabled={!clickable(s)}
                  title={s.seatNumber + ' - ' + s.seatClass + ' - ₹' + Number(s.price).toLocaleString('en-IN') + ' - ' + s.status}
                  onClick={() => onToggle && onToggle(s)}
                >
                  {s.seatNumber.replace(/^\d+/, '')}
                </button>
              </span>
            ))}
          </div>
        )
      })}
      <div className="legend">
        <span><i className="seat available" /> Available</span>
        <span><i className="seat available business" /> Business</span>
        <span><i className="seat selected" /> Selected</span>
        <span><i className="seat taken" /> Taken</span>
        <span><i className="seat blocked" /> Blocked</span>
      </div>
    </div>
  )
}
