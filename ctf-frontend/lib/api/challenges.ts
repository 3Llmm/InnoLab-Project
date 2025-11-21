import { apiClient } from './client'
import type { Challenge, CreateChallengeData } from '@/lib/types'

export async function getAllChallenges(): Promise<Challenge[]> {
  try {
    const response = await apiClient.get<Challenge[]>('/api/challenges')
    return response
  } catch (error) {
    console.error('Failed to fetch challenges:', error)
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
    if (challengeData.defaultSshPort) {
      formData.append('defaultSshPort', challengeData.defaultSshPort.toString())
    }
    if (challengeData.defaultVscodePort) {
      formData.append('defaultVscodePort', challengeData.defaultVscodePort.toString())
    }
    if (challengeData.defaultDesktopPort) {
      formData.append('defaultDesktopPort', challengeData.defaultDesktopPort.toString())
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
    if (challengeData.defaultSshPort) formData.append('defaultSshPort', challengeData.defaultSshPort.toString())
    if (challengeData.defaultVscodePort) formData.append('defaultVscodePort', challengeData.defaultVscodePort.toString())
    if (challengeData.defaultDesktopPort) formData.append('defaultDesktopPort', challengeData.defaultDesktopPort.toString())
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
    await apiClient.delete(`/api/challenges/${id}`)
  } catch (error) {
    console.error('Failed to delete challenge:', error)
    throw error
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