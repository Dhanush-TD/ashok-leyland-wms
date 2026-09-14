import React, { useState } from 'react'
import Sidebar from '../components/Sidebar.jsx'
import Topbar from '../components/Topbar.jsx'
import { retrievalApi } from '../api/client.js'

export default function Retrieval() {
  const [engineNumber, setEngineNumber] = useState('')
  const [plan, setPlan] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const [uploadResult, setUploadResult] = useState(null)
  const [uploadError, setUploadError] = useState('')

  async function handleSolve(e) {
    e.preventDefault()
    setError('')
    setPlan(null)
    setLoading(true)
    try {
      const res = await retrievalApi.solveRelocation(engineNumber)
      setPlan(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not compute a relocation plan for that engine.')
    } finally {
      setLoading(false)
    }
  }

  async function handleUpload(e) {
    const file = e.target.files[0]
    if (!file) return
    setUploadError('')
    setUploadResult(null)
    try {
      const res = await retrievalApi.uploadExcel(file)
      setUploadResult(res.data)
    } catch (err) {
      setUploadError(err.response?.data?.message || 'Could not process that file.')
    }
  }

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="main">
        <Topbar title="Retrieval Solver" subtitle="Compute the minimal-travel relocation plan for a target engine" wsConnected />
        <div className="content" style={{ maxWidth: 720 }}>
          <div className="panel">
            <div className="panel-title">Single Engine Lookup</div>
            <div className="panel-sub">Detects blockers in the aisle and sequences temp-move &rarr; extract &rarr; restore (FR-07)</div>
            {error && <div className="alert alert-error">{error}</div>}
            <form onSubmit={handleSolve}>
              <div className="field">
                <label>Target Engine Number</label>
                <input
                  className="text-input"
                  value={engineNumber}
                  onChange={(e) => setEngineNumber(e.target.value)}
                  placeholder="e.g. AL-ENG-2026-9041"
                  autoFocus
                />
              </div>
              <button className="btn btn-primary btn-block" disabled={loading || !engineNumber}>
                {loading ? 'Solving…' : 'Compute Relocation Plan'}
              </button>
            </form>
          </div>

          {plan && (
            <div className="panel" style={{ marginTop: 16 }}>
              <div className="panel-title">
                Plan for {plan.targetEngine}{' '}
                <span style={{ fontWeight: 400, color: 'var(--text-faint)', fontSize: 12 }}>
                  @ {plan.currentLocation}
                </span>
              </div>
              <div className="panel-sub">
                {plan.requiresRelocation
                  ? `${plan.blockersCount} blocking engine(s) detected — ${plan.sequence.length} step plan`
                  : 'No blockers detected — direct extraction'}
              </div>

              <div className="step-list">
                {plan.sequence.map((step) => (
                  <div className="step-card" key={step.step}>
                    <div className="step-index">{step.step}</div>
                    <div>
                      <div className={`step-phase ${step.phase}`}>{step.phase.replace(/_/g, ' ')}</div>
                      <div className="step-action">{step.action}</div>
                      <div className="step-route">{step.engineNumber} &middot; {step.from} &rarr; {step.to}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="panel" style={{ marginTop: 16 }}>
            <div className="panel-title">Bulk Dispatch Order</div>
            <div className="panel-sub">Upload an .xlsx order sheet — engine numbers in column A (FR-06)</div>
            {uploadError && <div className="alert alert-error">{uploadError}</div>}
            <input
              className="text-input"
              type="file"
              accept=".xlsx,.xls,.csv"
              onChange={handleUpload}
            />

            {uploadResult && (
              <div style={{ marginTop: 16 }}>
                <div className="field-row">
                  <span className="field-label">File</span>
                  <span className="field-value">{uploadResult.filename}</span>
                </div>
                <div className="field-row">
                  <span className="field-label">Total Rows</span>
                  <span className="field-value">{uploadResult.total_rows}</span>
                </div>
                <div className="field-row">
                  <span className="field-label">Valid Engines</span>
                  <span className="field-value" style={{ color: 'var(--status-available)' }}>
                    {uploadResult.valid_engines.length}
                  </span>
                </div>
                <div className="field-row">
                  <span className="field-label">Invalid Engines</span>
                  <span className="field-value" style={{ color: 'var(--status-blocked)' }}>
                    {uploadResult.invalid_engines.length}
                  </span>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
