import React, { useState } from 'react'
import Sidebar from '../components/Sidebar.jsx'
import Topbar from '../components/Topbar.jsx'
import { placementApi } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function Placement() {
  const { user } = useAuth()
  const [barcode, setBarcode] = useState('')
  const [scanResult, setScanResult] = useState(null)
  const [locationScan, setLocationScan] = useState('')
  const [message, setMessage] = useState(null)
  const [loading, setLoading] = useState(false)

  async function handleScan(e) {
    e.preventDefault()
    setMessage(null)
    setLoading(true)
    try {
      const res = await placementApi.scanEngine(barcode)
      setScanResult(res.data)
      setLocationScan('')
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Engine barcode not recognized.' })
      setScanResult(null)
    } finally {
      setLoading(false)
    }
  }

  async function handleConfirm(e) {
    e.preventDefault()
    setMessage(null)
    setLoading(true)
    try {
      const res = await placementApi.confirm(barcode, locationScan, user.userId)
      setMessage({ type: 'success', text: res.data.message })
      setScanResult(null)
      setBarcode('')
      setLocationScan('')
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Location mismatch — verify the scanned cell.' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="main">
        <Topbar title="Inbound Placement" subtitle="Scan an engine, then confirm at the recommended cell" wsConnected />
        <div className="content" style={{ maxWidth: 560 }}>
          {message && <div className={`alert alert-${message.type}`}>{message.text}</div>}

          <div className="panel">
            <div className="panel-title">Step 1 &middot; Scan Engine Barcode</div>
            <div className="panel-sub">Nearest available storage cell is auto-recommended (FR-04)</div>
            <form onSubmit={handleScan}>
              <div className="field">
                <label>Engine Barcode</label>
                <input
                  className="text-input"
                  value={barcode}
                  onChange={(e) => setBarcode(e.target.value)}
                  placeholder="e.g. AL-99012"
                  autoFocus
                />
              </div>
              <button className="btn btn-primary btn-block" disabled={loading || !barcode}>
                {loading ? 'Validating…' : 'Scan Engine'}
              </button>
            </form>
          </div>

          {scanResult && (
            <div className="panel" style={{ marginTop: 16 }}>
              <div className="panel-title">Step 2 &middot; Confirm At Location</div>
              <div className="panel-sub">
                Engine <strong>{scanResult.engineNumber}</strong> &rarr; recommended cell{' '}
                <strong style={{ color: 'var(--accent)' }}>{scanResult.recommendedLocation}</strong>{' '}
                (&asymp; {scanResult.distanceMeters}m from dock)
              </div>
              <form onSubmit={handleConfirm}>
                <div className="field">
                  <label>Scan Location Barcode To Confirm</label>
                  <input
                    className="text-input"
                    value={locationScan}
                    onChange={(e) => setLocationScan(e.target.value)}
                    placeholder={scanResult.recommendedLocation}
                  />
                </div>
                <button className="btn btn-primary btn-block" disabled={loading || !locationScan}>
                  {loading ? 'Confirming…' : 'Confirm Placement'}
                </button>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
