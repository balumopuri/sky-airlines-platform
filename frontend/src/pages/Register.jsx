import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'

export default function Register() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', email: '', password: '', phone: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const data = await api('/api/auth/register', { method: 'POST', body: form })
      login(data)
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="card form-narrow" onSubmit={submit}>
      <h2>Create account</h2>
      <label>Full name<input value={form.fullName} onChange={set('fullName')} required /></label>
      <label>Email<input type="email" value={form.email} onChange={set('email')} required /></label>
      <label>Phone<input value={form.phone} onChange={set('phone')} /></label>
      <label>Password (min 6 characters)<input type="password" minLength={6} value={form.password} onChange={set('password')} required /></label>
      {error && <p className="error">{error}</p>}
      <button className="btn" disabled={busy}>{busy ? 'Please wait...' : 'Register'}</button>
      <p className="muted">Already registered? <Link to="/login">Login</Link></p>
    </form>
  )
}
