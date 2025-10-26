import { apiClient } from './client'
import type { Challenge } from '@/lib/types'

// Backend response type
interface BackendChallenge {
  id: string
  name: string
  description: string
  fileUrl: string
}

// Map backend data to frontend format
function mapChallenge(backend: BackendChallenge): Challenge {
  // Derive category from ID (e.g., "web-101" -> "web-exploitation")
  const categoryMap: Record<string, string> = {
    'web': 'web-exploitation',
    'rev': 'reverse-engineering',
    'crypto': 'cryptography',
    'forensics': 'forensics',
    'pwn': 'binary-exploitation',
  }
  
  const prefix = backend.id.split('-')[0] || 'web'
  const category = categoryMap[prefix] || 'web-exploitation'
  
  // Derive difficulty from ID number
  const idNumber = parseInt(backend.id.split('-')[1] || '100')
  let difficulty: 'easy' | 'medium' | 'hard' = 'medium'
  if (idNumber < 200) difficulty = 'easy'
  else if (idNumber < 300) difficulty = 'medium'
  else difficulty = 'hard'
  
  // Calculate points
  const pointsMap = { easy: 100, medium: 200, hard: 300 }
  
  return {
    id: backend.id,
    title: backend.name, // Map "name" to "title"
    description: backend.description,
    category,
    difficulty,
    points: pointsMap[difficulty],
    solves: 0,
    solved: false,
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