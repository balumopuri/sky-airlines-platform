// One small helper for every call to the backend. It adds the login token automatically.
export async function api(path, { method = 'GET', body } = {}) {
  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = localStorage.getItem('token')
  if (token) headers['Authorization'] = 'Bearer ' + token

  let res
  try {
    res = await fetch(path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new Error('Cannot reach the server. Is the backend running on port 8080?')
  }

  const text = await res.text()
  let data = null
  try { data = text ? JSON.parse(text) : null } catch { data = null }

  if (!res.ok) {
    if (res.status === 401 && token) {
      // token expired or invalid -> log out
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
    throw new Error((data && data.message) || 'Request failed (' + res.status + ')')
  }
  return data
}

export const money = (n) => '₹' + Number(n || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })

export const fmtDateTime = (iso) =>
  iso ? new Date(iso).toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : ''

export const fmtDate = (iso) =>
  iso ? new Date(iso).toLocaleDateString('en-IN', { weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' }) : ''

export const fmtTime = (iso) =>
  iso ? new Date(iso).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' }) : ''

export const fmtDuration = (m) => Math.floor(m / 60) + 'h ' + (m % 60) + 'm'

export const todayStr = () => {
  const d = new Date()
  const p = (x) => String(x).padStart(2, '0')
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate())
}
