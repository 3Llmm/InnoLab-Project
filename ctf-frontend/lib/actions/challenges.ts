'use server'

import { apiClient } from '@/lib/api/client'

// Replace your submitFlag function with this:
export async function submitFlag(challengeId: string, flag: string) {
  try {
    const response = await apiClient.post<{ correct: boolean; message: string }>(
      '/api/flags/submit',
      { challengeId, flag }
    )
    
    return {
      success: response.correct,
      message: response.message,
    }
  } catch (error) {
    return {
      success: false,
      message: error instanceof Error ? error.message : 'Failed to submit flag',
    }
  }
}