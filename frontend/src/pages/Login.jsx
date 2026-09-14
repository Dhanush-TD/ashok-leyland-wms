import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(username, password)
      navigate('/map')
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid username or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-screen">
      <div className="login-card">
        <div className="login-header">
          <div className="login-mark">AL</div>
          <div className="login-title">Engine Warehouse Control</div>
          <div className="login-sub">Ashok Leyland &middot; Sign in to continue</div>
        </div>

        <div className="panel">
          {error && <div className="alert alert-error">{error}</div>}
          <form onSubmit={handleSubmit}>
            <div className="field">
              <label>Username</label>
              <input
                className="text-input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="operator1"
                autoFocus
              />
            </div>
            <div className="field">
              <label>Password</label>
              <input
                className="text-input"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;"
              />
            </div>
            <button className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Signing in…' : 'Sign In'}
            </button>
          </form>

          <div className="demo-creds">
            demo accounts (password: Password123)<br />
            admin &middot; supervisor1 &middot; operator1
          </div>
        </div>
      </div>
    </div>
  )
}
