import React from 'react'

export default function Topbar({ title, subtitle, wsConnected }) {
  return (
    <header className="topbar">
      <div>
        <div className="topbar-title">{title}</div>
        {subtitle && <div className="topbar-sub">{subtitle}</div>}
      </div>
      <div className="conn-status">
        <span className={`conn-dot ${wsConnected ? '' : 'offline'}`} />
        {wsConnected ? 'Live updates connected' : 'Live updates offline'}
      </div>
    </header>
  )
}
