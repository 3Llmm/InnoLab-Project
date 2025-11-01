import { apiClient } from './client'
import type { Challenge } from '@/lib/types'

interface BackendChallenge {
  id: string
  title: string                    
  description: string
  category: string                 
  difficulty: string               
  points: number                   
  fileUrl: string
}

// Map backend data to frontend format
function mapChallenge(backend: BackendChallenge): Challenge {
  return {
    id: backend.id,
    title: backend.title,                    
    description: backend.description,        
    category: backend.category as Challenge['category'], 
    difficulty: backend.difficulty as Challenge['difficulty'], 
    points: backend.points,                  
    solved: false,            
    fileurl: backend.fileUrl,               
    
  }
}

// Fetch real challenges from backend
export async function getAllChallenges(): Promise<Challenge[]> {
  try {
    const response = await apiClient.get<BackendChallenge[]>('/api/challenges')
    return response.map(mapChallenge)
  } catch (error) {
    console.error('Failed to fetch challenges:', error)
    // Return empty array on error instead of crashing
    return []
  }
}

// Get single challenge by ID
export async function getChallengeById(id: string): Promise<Challenge | null> {
  try {
    const challenges = await getAllChallenges()
    return challenges.find((c) => c.id === id) || null
  } catch (error) {
    console.error('Failed to fetch challenge:', error)
    return null
  }
}

// Get challenges by category
export async function getChallengesByCategory(category: string): Promise<Challenge[]> {
  try {
    const challenges = await getAllChallenges()
    return challenges.filter((c) => c.category === category)
  } catch (error) {
    console.error('Failed to fetch challenges:', error)
    return []
  }
}