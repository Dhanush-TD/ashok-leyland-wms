import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext.jsx'
import Login from './pages/Login.jsx'
import WarehouseMap from './pages/WarehouseMap.jsx'
import Placement from './pages/Placement.jsx'
import Retrieval from './pages/Retrieval.jsx'

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return children
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/map" element={<ProtectedRoute><WarehouseMap /></ProtectedRoute>} />
      <Route path="/placement" element={<ProtectedRoute><Placement /></ProtectedRoute>} />
      <Route path="/retrieval" element={<ProtectedRoute><Retrieval /></ProtectedRoute>} />
      <Route path="*" element={<Navigate to="/map" replace />} />
    </Routes>
  )
}
