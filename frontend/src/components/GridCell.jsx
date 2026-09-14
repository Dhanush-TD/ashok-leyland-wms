import React from 'react'

export default function GridCell({ location, onClick, highlighted }) {
  return (
    <div
      className={`grid-cell status-${location.status} ${highlighted ? 'pulse' : ''}`}
      onClick={() => onClick(location)}
      title={`${location.locationCode} — ${location.status}`}
    >
      {location.colNumber}
    </div>
  )
}
