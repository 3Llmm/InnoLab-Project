'use client'
import { useState, useEffect, createContext, useContext } from 'react'
import { apiClient } from '@/lib/api/client'
import type { AuthState, LoginCredentials, ApiResult } from '@/lib/types'

interface AuthContextType {
  auth: AuthState
  login: (credentials: LoginCredentials) => Promise<ApiResult>
  logout: () => Promise<void>
  isLoading: boolean
  checkAuth: () => Promise<void>
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

  // Clear all cookies on app startup
  useEffect(() => {
    document.cookie.split(";").forEach((c) => {
      document.cookie = c
        .replace(/^ +/, "")
        .replace(/=.*/, `=;expires=${new Date().toUTCString()};path=/`);
    });
  }, []) // Empty array = runs once on mount

  const checkAuth = async () => {
    try {
      const userInfo = await apiClient.get<{ username: string; status: string }>('/api/user/me')
      if (userInfo.status === 'success') {
        setAuth({
          token: 'http-only-cookie',
          user: userInfo.username,
          isAuthenticated: true,
        })
      } else {
        throw new Error('Not authenticated')
      }
    } catch (error) {
      setAuth({
        token: null,
        user: null,
        isAuthenticated: false,
      })
    }
  }

  useEffect(() => {
    checkAuth().finally(() => setIsLoading(false))
  }, [])

  const login = async (credentials: LoginCredentials): Promise<ApiResult> => {
    setIsLoading(true)
    try {
      const response = await apiClient.post<{ 
        status: string; 
        message: string; 
        username: string 
      }>('/api/login', {
        username: credentials.username,
        password: credentials.password,
      })

      if (response.status === 'success') {
        await checkAuth()
        return { success: true, data: response }
      }

      return { success: false, error: 'Login failed' }
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Login failed',
      }
    } finally {
      setIsLoading(false)
    }
  }

  const logout = async () => {
    try {
      await apiClient.post('/api/logout',{})
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      setAuth({
        token: null,
        user: null,
        isAuthenticated: false,
      })
    }
  }

  const value = {
    auth,
    login,
    logout,
    isLoading,
    checkAuth,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}