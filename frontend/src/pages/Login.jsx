import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const data = await api('/api/auth/login', { method: 'POST', body: { email, password } })
      login(data)
      const back = location.state && location.state.from
      navigate(data.role === 'ADMIN' ? '/admin' : back || '/')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="card form-narrow" onSubmit={submit}>
      <h2>Login</h2>
      <label>Email<input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>
      <label>Password<input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required /></label>
      {error && <p className="error">{error}</p>}
      <button className="btn" disabled={busy}>{busy ? 'Please wait...' : 'Login'}</button>
      <p className="muted">New here? <Link to="/register">Create an account</Link></p>
      <p className="muted small">Admins also log in here. Demo: admin@airline.com / admin123, customer: demo@airline.com / demo123</p>
    </form>
  )
}
