import { apiClient } from './client'
import type { Challenge, CreateChallengeData } from '@/lib/types'

export async function getAllChallenges(): Promise<Challenge[]> {
  try {
    const response = await apiClient.get<Challenge[]>('/api/challenges')
    return response
  } catch (error) {
    console.error('Failed to fetch challenges:', error)
    // Check if this is an authentication error (shouldn't happen for GET requests now)
    if (error instanceof Error && error.message.includes('403')) {
      console.warn('Unexpected authentication error for challenges endpoint')
      // Fallback: try to show user-friendly message
      throw new Error('Failed to load challenges. Please try again later.')
    }
    // Handle other errors
    if (error instanceof Error) {
      throw new Error('Failed to load challenges. Please try again later.')
    }
    throw error
  }
}

export async function getChallenge(id: string): Promise<Challenge> {
  try {
    const response = await apiClient.get<Challenge>(`/api/challenges/${id}`)
    return response
  } catch (error) {
    console.error(`Failed to fetch challenge ${id}:`, error)
    throw error
  }
}
export async function getChallengesByCategory(category: string): Promise<Challenge[]> {
  const allChallenges = await getAllChallenges();
  return allChallenges.filter(challenge => 
    challenge.category.toLowerCase().includes(category.toLowerCase()) ||
    category.toLowerCase().includes(challenge.category.toLowerCase())
  );
}

export async function createChallenge(challengeData: CreateChallengeData): Promise<Challenge> {
  try {
    const formData = new FormData()
    
    // Append basic fields
    formData.append('title', challengeData.title)
    formData.append('description', challengeData.description)
    formData.append('category', challengeData.category)
    formData.append('difficulty', challengeData.difficulty)
    formData.append('points', challengeData.points.toString())
    formData.append('flag', challengeData.flag)
    formData.append('file', challengeData.file)
    
    // Append instance fields if provided
    if (challengeData.dockerImageName) {
      formData.append('dockerImageName', challengeData.dockerImageName)
    }
    if (challengeData.requiresInstance !== undefined) {
      formData.append('requiresInstance', challengeData.requiresInstance.toString())
    }

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/challenges`,
      {
        method: 'POST',
        body: formData,
        credentials: 'include',
      }
    )

    if (!response.ok) {
      throw new Error(`Failed to create challenge: ${response.status}`)
    }

    return response.json()
  } catch (error) {
    console.error('Failed to create challenge:', error)
    throw error
  }
}

export async function updateChallenge(id: string, challengeData: Partial<CreateChallengeData>): Promise<Challenge> {
  try {
    const formData = new FormData()
    
    // Append only provided fields
    if (challengeData.title) formData.append('title', challengeData.title)
    if (challengeData.description) formData.append('description', challengeData.description)
    if (challengeData.category) formData.append('category', challengeData.category)
    if (challengeData.difficulty) formData.append('difficulty', challengeData.difficulty)
    if (challengeData.points) formData.append('points', challengeData.points.toString())
    if (challengeData.flag) formData.append('flag', challengeData.flag)
    if (challengeData.file) formData.append('file', challengeData.file)
    if (challengeData.dockerImageName) formData.append('dockerImageName', challengeData.dockerImageName)
    if (challengeData.requiresInstance !== undefined) {
      formData.append('requiresInstance', challengeData.requiresInstance.toString())
    }

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/challenges/${id}`,
      {
        method: 'PUT',
        body: formData,
        credentials: 'include',
      }
    )

    if (!response.ok) {
      throw new Error(`Failed to update challenge: ${response.status}`)
    }

    return response.json()
  } catch (error) {
    console.error('Failed to update challenge:', error)
    throw error
  }
}

export async function deleteChallenge(id: string): Promise<void> {
  try {
    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/challenges/${id}`,
      {
        method: 'DELETE',
        credentials: 'include',
      }
    )
    
    // Handle 204 No Content - successful deletion
    if (response.status === 204) {
      return
    }
    
    // Handle 404 Not Found
    if (response.status === 404) {
      throw new Error('Challenge not found. It may have already been deleted.')
    }
    
    // Handle other non-ok responses
    if (!response.ok) {
      throw new Error(`Failed to delete challenge: ${response.status}`)
    }
    
    // Try to parse response if there's content
    const contentLength = response.headers.get('content-length')
    if (contentLength && contentLength !== '0') {
      const text = await response.text()
      if (text) {
        return JSON.parse(text)
      }
    }
    
    return
  } catch (error) {
    console.error('Failed to delete challenge:', error)
    if (error instanceof Error) {
      throw error
    }
    throw new Error('Failed to delete challenge. Please try again.')
  }
}

export async function downloadChallengeFile(id: string): Promise<Blob> {
  try {
    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/challenges/${id}/download`,
      {
        method: 'GET',
        credentials: 'include',
      }
    )

    if (!response.ok) {
      throw new Error(`Failed to download file: ${response.status}`)
    }

    return response.blob()
  } catch (error) {
    console.error('Failed to download challenge file:', error)
    throw error
  }
}