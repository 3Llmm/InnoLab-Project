'use server'

import { apiClient } from '@/lib/api/client'
import type { LoginCredentials, LoginResponse, ApiResult } from '@/lib/types'

export async function loginUser(formData: FormData): Promise<ApiResult> {
  try {
    const credentials: LoginCredentials = {
      username: formData.get('username') as string,
      password: formData.get('password') as string,
    }

    console.log('Attempting login with:', credentials.username);

    // Call your Spring Boot backend
    const response: LoginResponse = await apiClient.post<LoginResponse>('/api/login', credentials)

    console.log('Login response:', response);

    // Store the JWT token
    if (response.token && typeof window !== 'undefined') {
      localStorage.setItem('auth_token', response.token)
      localStorage.setItem('auth_user', credentials.username)
    }

    return {
      success: true,
      data: response
    }
  } catch (error) {
    console.error('Login error:', error)
    return {
      success: false,
      error: error instanceof Error ? error.message : 'Login failed. Please check your credentials.'
    }
  }
}

export async function logoutUser(): Promise<void> {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
  }
}

export async function getStoredToken(): Promise<string | null> {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('auth_token')
  }
  return null
}

export async function getStoredUser(): Promise<string | null> {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('auth_user')
  }
  return null
}