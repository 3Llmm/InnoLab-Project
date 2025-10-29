'use client'

import { useState, useEffect, createContext, useContext } from 'react'
import { apiClient } from '@/lib/api/client' // ✅ CHANGED: Import apiClient instead
import type { AuthState, LoginCredentials, ApiResult } from '@/lib/types'

interface AuthContextType {
  auth: AuthState
  login: (credentials: LoginCredentials) => Promise<ApiResult>
  logout: () => void
  isLoading: boolean
}

const AuthContext = createContext<AuthContextType | null>(null)

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [auth, setAuth] = useState<AuthState>({
    token: null,
    user: null,
    isAuthenticated: false,
  })
  const [isLoading, setIsLoading] = useState(true)

  // ✅ CHANGED: Load from localStorage directly (client-side only)
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('auth_token')
      const user = localStorage.getItem('auth_user')
      
      if (token && user) {
        setAuth({
          token,
          user,
          isAuthenticated: true,
        })
      }
    }
    setIsLoading(false)
  }, [])

  // ✅ CHANGED: Call backend directly with apiClient
  const login = async (credentials: LoginCredentials): Promise<ApiResult> => {
    setIsLoading(true)
    try {
      // Call backend API
      const response = await apiClient.post<{ token: string }>('/api/login', {
        username: credentials.username,
        password: credentials.password,
      })

      // Store token in localStorage
      if (response.token) {
        localStorage.setItem('auth_token', response.token)
        localStorage.setItem('auth_user', credentials.username)

        setAuth({
          token: response.token,
          user: credentials.username,
          isAuthenticated: true,
        })

        return { success: true, data: response }
      }

      return { success: false, error: 'No token received' }
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Login failed',
      }
    } finally {
      setIsLoading(false)
    }
  }

  // ✅ CHANGED: Clear localStorage directly
  const logout = async () => {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
    
    setAuth({
      token: null,
      user: null,
      isAuthenticated: false,
    })
  }

  const value = {
    auth,
    login,
    logout,
    isLoading,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}