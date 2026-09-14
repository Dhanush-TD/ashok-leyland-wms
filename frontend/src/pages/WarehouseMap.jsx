import React, { useEffect, useMemo, useRef, useState } from 'react'
import Sidebar from '../components/Sidebar.jsx'
import Topbar from '../components/Topbar.jsx'
import GridCell from '../components/GridCell.jsx'
import CellDrawer from '../components/CellDrawer.jsx'
import { warehouseApi } from '../api/client.js'

const LEGEND = [
  { status: 'AVAILABLE', label: 'Available', color: 'var(--bg-panel-raised)' },
  { status: 'OCCUPIED', label: 'Occupied', color: 'var(--status-occupied)' },
  { status: 'PENDING_CONFIRMATION', label: 'Pending Confirm', color: 'var(--status-pending)' },
  { status: 'RESERVED_TEMP', label: 'Reserved (Temp)', color: 'var(--status-reserved)' },
  { status: 'RELOCATING', label: 'Relocating', color: 'var(--status-relocating)' },
  { status: 'BLOCKED', label: 'Blocked', color: 'var(--status-blocked)' },
]

export default function WarehouseMap() {
  const [mapData, setMapData] = useState(null)
  const [selected, setSelected] = useState(null)
  const [search, setSearch] = useState('')
  const [wsConnected, setWsConnected] = useState(false)
  const [error, setError] = useState('')
  const [highlighted, setHighlighted] = useState(null)
  const wsRef = useRef(null)

  async function loadMap() {
    try {
      const res = await warehouseApi.getMap()
      setMapData(res.data)
    } catch (err) {
      setError('Could not load the warehouse map. Is the backend running on :8080?')
    }
  }

  useEffect(() => {
    loadMap()

const wsUrl =
  import.meta.env.VITE_WS_URL ||
  `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws/warehouse-updates`
    try {
      const ws = new WebSocket(wsUrl)
      wsRef.current = ws
      ws.onopen = () => setWsConnected(true)
      ws.onclose = () => setWsConnected(false)
      ws.onerror = () => setWsConnected(false)
      ws.onmessage = (event) => {
        try {
          const msg = JSON.parse(event.data)
          if (msg.type === 'LOCATION_UPDATE') {
            setMapData((prev) => {
              if (!prev) return prev
              const grid = prev.grid.map((loc) =>
                loc.locationCode === msg.payload.locationCode ? { ...loc, ...msg.payload } : loc
              )
              return { ...prev, grid }
            })
            setHighlighted(msg.payload.locationCode)
            setTimeout(() => setHighlighted(null), 2000)
          }
        } catch { /* ignore malformed frames */ }
      }
    } catch {
      setWsConnected(false)
    }

    return () => wsRef.current?.close()
  }, [])

  const rows = useMemo(() => {
    if (!mapData) return []
    const byRow = {}
    for (const loc of mapData.grid) {
      if (!byRow[loc.rowCode]) byRow[loc.rowCode] = []
      byRow[loc.rowCode].push(loc)
    }
    return Object.entries(byRow).sort(([a], [b]) => a.localeCompare(b))
  }, [mapData])

  const maxCols = useMemo(() => {
    if (!mapData) return 12
    return Math.max(...mapData.grid.map((l) => l.colNumber), 12)
  }, [mapData])

  async function openCell(loc) {
    try {
      const res = await warehouseApi.getLocation(loc.locationCode)
      setSelected(res.data)
    } catch {
      setSelected(loc)
    }
  }

  const filteredMatch = search.trim().toUpperCase()

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="main">
        <Topbar title="Warehouse Map" subtitle="Live cinema-grid view of every storage cell" wsConnected={wsConnected} />
        <div className="content">
          {error && <div className="alert alert-error">{error}</div>}

          {mapData && (
            <div className="stat-strip">
              <div className="stat-card">
                <div className="stat-value">{mapData.totalLocations}</div>
                <div className="stat-label">Total Cells</div>
              </div>
              <div className="stat-card available">
                <div className="stat-value">{mapData.available}</div>
                <div className="stat-label">Available</div>
              </div>
              <div className="stat-card occupied">
                <div className="stat-value">{mapData.occupied}</div>
                <div className="stat-label">Occupied</div>
              </div>
              <div className="stat-card pending">
                <div className="stat-value">{mapData.pending}</div>
                <div className="stat-label">Pending</div>
              </div>
              <div className="stat-card reserved">
                <div className="stat-value">{mapData.reservedTemp}</div>
                <div className="stat-label">Reserved Temp</div>
              </div>
            </div>
          )}

          <div className="map-panel">
            <div className="map-panel-head">
              <div className="map-title">Storage Bay &middot; Rows A&ndash;H</div>
              <input
                className="search-input"
                placeholder="Jump to cell e.g. C7"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>

            <div className="legend">
              {LEGEND.map((l) => (
                <div key={l.status} className="legend-item">
                  <span className="legend-swatch" style={{ background: l.color }} />
                  {l.label}
                </div>
              ))}
            </div>

            <div className="grid-wrap" style={{ marginTop: 18 }}>
              {rows.map(([rowCode, cells]) => (
                <div
                  key={rowCode}
                  className="cinema-grid"
                  style={{ gridTemplateColumns: `24px repeat(${maxCols}, 1fr)`, marginBottom: 4 }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--text-faint)' }}>
                    {rowCode}
                  </div>
                  {cells
                    .sort((a, b) => a.colNumber - b.colNumber)
                    .map((cell) => (
                      <GridCell
                        key={cell.locationCode}
                        location={cell}
                        onClick={openCell}
                        highlighted={
                          highlighted === cell.locationCode ||
                          (filteredMatch && cell.locationCode === filteredMatch)
                        }
                      />
                    ))}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {selected && <CellDrawer location={selected} onClose={() => setSelected(null)} />}
    </div>
  )
}
