import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'

export default function Profile() {
  const { user, login } = useAuth()
  const [form, setForm] = useState({ fullName: '', phone: '' })
  const [email, setEmail] = useState('')
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    api('/api/users/me').then((u) => { setForm({ fullName: u.fullName, phone: u.phone || '' }); setEmail(u.email) })
      .catch((e) => setError(e.message))
  }, [])

  const save = async (e) => {
    e.preventDefault()
    setMsg(''); setError('')
    try {
      const u = await api('/api/users/me', { method: 'PUT', body: form })
      login({ ...user, fullName: u.fullName })
      setMsg('Profile updated')
    } catch (err) { setError(err.message) }
  }

  return (
    <form className="card form-narrow" onSubmit={save}>
      <h2>My profile</h2>
      <label>Email<input value={email} disabled /></label>
      <label>Full name<input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} required /></label>
      <label>Phone<input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} /></label>
      {msg && <p className="ok">{msg}</p>}
      {error && <p className="error">{error}</p>}
      <button className="btn">Save</button>
    </form>
  )
}
