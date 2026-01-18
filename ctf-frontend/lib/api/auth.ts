// API functions for authentication and user information
import { apiClient } from './client'
import type { ApiResult } from '@/lib/types'

export interface UserInfo {
  username: string
  email: string
  createdAt?: string
  isAdmin?: boolean
}

/**
 * Get information about the currently authenticated user
 */
export async function getUserInfo(): Promise<ApiResult<UserInfo>> {
  try {
    const response = await apiClient.get('/api/auth/me')
    return {
      success: true,
      data: response.data
    }
  } catch (error) {
    console.error('Error fetching user info:', error)
    return {
      success: false,
      error: error instanceof Error ? error.message : 'Failed to fetch user information'
    }
  }
}

/**
 * Check if the current user is an admin
 */
export async function isAdmin(): Promise<ApiResult<{ isAdmin: boolean }>> {
  try {
    const response = await apiClient.get('/api/auth/admin-check')
    return {
      success: true,
      data: { isAdmin: response.data.isAdmin || false }
    }
  } catch (error) {
    console.error('Error checking admin status:', error)
    return {
      success: false,
      error: error instanceof Error ? error.message : 'Failed to check admin status',
      data: { isAdmin: false }
    }
  }
}