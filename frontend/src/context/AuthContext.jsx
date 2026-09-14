import React, { createContext, useContext, useState, useCallback } from 'react'
import { authApi } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('wms_user')
    return stored ? JSON.parse(stored) : null
  })

  const login = useCallback(async (username, password) => {
    const res = await authApi.login(username, password)
    const { accessToken, user: loggedInUser } = res.data
    localStorage.setItem('wms_token', accessToken)
    localStorage.setItem('wms_user', JSON.stringify(loggedInUser))
    setUser(loggedInUser)
    return loggedInUser
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('wms_token')
    localStorage.removeItem('wms_user')
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
