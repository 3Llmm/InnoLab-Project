// API functions for fetching scoreboard data
// TODO: Replace mock data with actual API calls to your backend

import type { ScoreboardEntry } from "@/lib/types"

// Mock scoreboard data
const MOCK_SCOREBOARD: ScoreboardEntry[] = [
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

export async function getScoreboard(): Promise<ScoreboardEntry[]> {
  // TODO: Replace with actual API call
  // Example: const response = await fetch('/api/scoreboard')
  // return response.json()

  return MOCK_SCOREBOARD
}
