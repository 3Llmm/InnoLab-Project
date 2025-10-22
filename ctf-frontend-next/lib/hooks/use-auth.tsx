'use client'

import { useState, useEffect, createContext, useContext } from 'react'
import { loginUser, logoutUser, getStoredToken, getStoredUser } from '@/lib/actions/auth'
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

  // Check for existing auth on app start
  useEffect(() => {
    async function loadStoredAuth() {
      try {
        const [token, user] = await Promise.all([
          getStoredToken(),
          getStoredUser(),
        ])

        if (token && user) {
          setAuth({
            token,
            user,
            isAuthenticated: true,
          })
        }
      } catch (error) {
        console.error('Failed to load stored auth:', error)
        await logoutUser()
      } finally {
        setIsLoading(false)
      }
    }

    loadStoredAuth()
  }, [])

  const login = async (credentials: LoginCredentials): Promise<ApiResult> => {
    setIsLoading(true)
    try {
      const formData = new FormData()
      formData.append('username', credentials.username)
      formData.append('password', credentials.password)

      const result = await loginUser(formData)

      if (result.success && result.data) {
        setAuth({
          token: result.data.token,
          user: credentials.username,
          isAuthenticated: true,
        })
      }

      return result
    } catch (error) {
      const errorResult: ApiResult = {
        success: false,
        error: error instanceof Error ? error.message : 'Login failed',
      }
      return errorResult
    } finally {
      setIsLoading(false)
    }
  }

  const logout = async () => {
    setIsLoading(true)
    try {
      await logoutUser()
      setAuth({
        token: null,
        user: null,
        isAuthenticated: false,
      })
    } finally {
      setIsLoading(false)
    }
  }

  const value = {
    auth,
    login,
    logout,
    isLoading,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}