export interface AdminStats {
  totalChallenges: number
  totalUsers: number | string
  totalSubmissions: number | string
  activeChallenges: number
  challengesByCategory: Array<{ category: string; count: number }>
  challengesByDifficulty: Array<{ difficulty: string; count: number }>
}

export async function getAdminStats(): Promise<AdminStats> {
  try {
    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/challenges/admin/stats`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      }
    )

    if (!response.ok) {
      throw new Error(`Failed to fetch stats: ${response.status}`)
    }

    const backendStats = await response.json()
    
    return {
      totalChallenges: typeof backendStats.totalChallenges === 'number' ? backendStats.totalChallenges : 0,
      totalUsers: typeof backendStats.totalUsers === 'number' ? backendStats.totalUsers : "N/A",
      totalSubmissions: typeof backendStats.totalSubmissions === 'number' ? backendStats.totalSubmissions : "N/A",
      activeChallenges: typeof backendStats.activeChallenges === 'number' ? backendStats.activeChallenges : 0,
      challengesByCategory: Array.isArray(backendStats.challengesByCategory) 
        ? backendStats.challengesByCategory.map((cat: any) => ({
            category: String(cat.category || ''),
            count: typeof cat.count === 'number' ? cat.count : 0
          }))
        : [],
      challengesByDifficulty: Array.isArray(backendStats.challengesByDifficulty)
        ? backendStats.challengesByDifficulty.map((diff: any) => ({
            difficulty: String(diff.difficulty || ''),
            count: typeof diff.count === 'number' ? diff.count : 0
          }))
        : [],
    }
  } catch (error) {
    console.error('Failed to fetch admin stats:', error)
    throw error 
  }
}