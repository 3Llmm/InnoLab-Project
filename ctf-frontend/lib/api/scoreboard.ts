// API functions for fetching scoreboard data
// Using real API calls to the backend

import type { ScoreboardEntry } from "@/lib/types"
import { getTopSolvers } from "./solves"
import { getUserStatistics } from "./solves"
import { getUserInfo } from "./auth"

/**
 * Get scoreboard data from the backend
 * Uses solve count as the primary ranking metric
 */
export async function getScoreboard(): Promise<ScoreboardEntry[]> {
  try {
    // Get top solvers from the backend
    const topSolversResponse = await getTopSolvers(50) // Get top 50 solvers
    
    if (!topSolversResponse.success || !topSolversResponse.data) {
      throw new Error('Failed to fetch scoreboard data')
    }
    
    // Convert the data to ScoreboardEntry format
    return topSolversResponse.data
      .filter(entry => entry.username) // Filter out invalid entries
      .map((entry, index) => ({
        id: (index + 1).toString(),
        username: entry.username || 'Unknown User',
        score: (entry.solveCount || 0) * 100, // Simple scoring: 100 points per solve
        solvedChallenges: entry.solveCount || 0
      }))
    
  } catch (error) {
    console.error('Error fetching scoreboard:', error)
    
    // Fallback to mock data if API fails
    return getMockScoreboard()
  }
}

// Mock scoreboard data as fallback
export function getMockScoreboard(): ScoreboardEntry[] {
  return [
    { id: "1", username: "h4ck3r_pr0", score: 2500, solvedChallenges: 25 },
    { id: "2", username: "cyber_ninja", score: 2350, solvedChallenges: 23 },
    { id: "3", username: "sec_master", score: 2100, solvedChallenges: 21 },
    { id: "4", username: "code_breaker", score: 1950, solvedChallenges: 19 },
    { id: "5", username: "exploit_dev", score: 1800, solvedChallenges: 18 },
    { id: "6", username: "crypto_king", score: 1650, solvedChallenges: 16 },
    { id: "7", username: "forensics_fan", score: 1500, solvedChallenges: 15 },
    { id: "8", username: "web_warrior", score: 1350, solvedChallenges: 13 },
    { id: "9", username: "binary_boss", score: 1200, solvedChallenges: 12 },
    { id: "10", username: "reverse_eng", score: 1050, solvedChallenges: 10 },
  ]
}
