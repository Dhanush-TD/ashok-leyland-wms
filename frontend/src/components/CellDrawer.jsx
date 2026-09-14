import React from 'react'

export default function CellDrawer({ location, onClose }) {
  if (!location) return null

  return (
    <div className="drawer-overlay" onClick={onClose}>
      <div className="drawer" onClick={(e) => e.stopPropagation()}>
        <div className="drawer-head">
          <div>
            <div className="drawer-code">{location.locationCode}</div>
            <span className={`badge status-${location.status}`}>{location.status.replace(/_/g, ' ')}</span>
          </div>
          <button className="close-btn" onClick={onClose}>&times;</button>
        </div>

        <div className="field-row">
          <span className="field-label">Row</span>
          <span className="field-value">{location.rowCode}</span>
        </div>
        <div className="field-row">
          <span className="field-label">Column</span>
          <span className="field-value">{location.colNumber}</span>
        </div>

        {location.engine ? (
          <>
            <div style={{ marginTop: 18, marginBottom: 10, fontSize: 12, color: 'var(--text-faint)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Stored Engine
            </div>
            <div className="field-row">
              <span className="field-label">Engine No.</span>
              <span className="field-value">{location.engine.engineNumber}</span>
            </div>
            <div className="field-row">
              <span className="field-label">Barcode</span>
              <span className="field-value">{location.engine.barcode}</span>
            </div>
            <div className="field-row">
              <span className="field-label">Model</span>
              <span className="field-value">{location.engine.model}</span>
            </div>
            <div className="field-row">
              <span className="field-label">Batch</span>
              <span className="field-value">{location.engine.batchNumber}</span>
            </div>
            <div className="field-row">
              <span className="field-label">Mfg Date</span>
              <span className="field-value">{location.engine.mfgDate}</span>
            </div>
            <div className="field-row">
              <span className="field-label">Status</span>
              <span className="field-value">{location.engine.status}</span>
            </div>
            <div className="field-row">
              <span className="field-label">Last Movement</span>
              <span className="field-value">{location.engine.lastMovement || '—'}</span>
            </div>
          </>
        ) : (
          <div className="empty-state">This cell is empty and ready to receive an engine.</div>
        )}
      </div>
    </div>
  )
}
