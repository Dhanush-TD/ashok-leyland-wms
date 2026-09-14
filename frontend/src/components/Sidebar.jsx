import React from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const NAV_ITEMS = [
  { path: '/map', label: 'Warehouse Map' },
  { path: '/placement', label: 'Inbound Placement' },
  { path: '/retrieval', label: 'Retrieval Solver' },
]

export default function Sidebar() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, logout } = useAuth()

  const initials = (user?.fullName || user?.username || '??')
    .split(' ')
    .map((p) => p[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">AL</div>
        <div className="brand-name">Engine WMS</div>
        <div className="brand-sub">Ashok Leyland &middot; Ops Console</div>
      </div>

      <nav className="nav-list">
        {NAV_ITEMS.map((item) => (
          <button
            key={item.path}
            className={`nav-item ${location.pathname === item.path ? 'active' : ''}`}
            onClick={() => navigate(item.path)}
          >
            <span className="nav-dot" />
            {item.label}
          </button>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="user-chip">
          <div className="user-avatar">{initials}</div>
          <div className="user-meta">
            <span className="user-name">{user?.fullName || user?.username}</span>
            <span className="user-role">{user?.role}</span>
          </div>
        </div>
        <button className="logout-btn" onClick={() => { logout(); navigate('/login') }}>
          Sign out
        </button>
      </div>
    </aside>
  )
}
