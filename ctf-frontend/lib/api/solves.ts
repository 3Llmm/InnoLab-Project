// API functions for solve tracking
import { apiClient } from './client'
import type { ApiResult } from '@/lib/types'

// Type definitions for solve tracking
export interface Solve {
    id: number
    username: string
    challengeId: string
    challengeTitle: string
    pointsEarned: number
    solvedAt: string
    category: string
    difficulty: string
}

export interface SolveStatistics {
    totalSolves: number
    totalPoints: number
    categoryDistribution: Record<string, number>
    difficultyDistribution: Record<string, number>
    challengesSolved: number
}

export interface ChallengeStatistics {
    challengeId: string
    challengeTitle: string
    solveCount: number
    category: string
    difficulty: string
    points: number
    solveRate: string
}

export interface LeaderboardEntry {
    username: string
    solveCount: number
}

/**
 * Get all challenges solved by the authenticated user
 */
export async function getMySolves(): Promise<ApiResult<Solve[]>> {
    try {
        const response = await apiClient.get('/api/solves/me')
        return {
            success: true,
            data: response.data
        }
    } catch (error) {
        console.error('Error fetching user solves:', error)
        return {
            success: false,
            error: error instanceof Error ? error.message : 'Failed to fetch solves'
        }
    }
}

/**
 * Check if the authenticated user has solved a specific challenge
 */
export async function checkIfSolved(challengeId: string): Promise<ApiResult<{ solved: boolean }>> {
    try {
        const response = await apiClient.get(`/api/solves/check/${challengeId}`)
        return {
            success: true,
            data: response.data
        }
    } catch (error) {
        console.error('Error checking solve status:', error)
        return {
            success: false,
            error: error instanceof Error ? error.message : 'Failed to check solve status'
        }
    }
}

/**
 * Get the number of users who solved a specific challenge
 */
export async function getSolveCountForChallenge(challengeId: string): Promise<ApiResult<{ count: number }>> {
    try {
        const response = await apiClient.get(`/api/solves/challenge/${challengeId}/count`)
        return {
            success: true,
            data: response.data
        }
    } catch (error) {
        console.error('Error fetching solve count:', error)
        return {
            success: false,
            error: error instanceof Error ? error.message : 'Failed to fetch solve count'
        }
    }
}

/**
 * Get recent solves (for activity feed)
 */
export async function getRecentSolves(limit: number = 10): Promise<ApiResult<Solve[]>> {
    try {
        const response = await apiClient.get('/api/solves/recent', {
            params: { limit }
        })
        return {
            success: true,
            data: response.data
        }
    } catch (error) {
        console.error('Error fetching recent solves:', error)
        return {
            success: false,
            error: error instanceof Error ? error.message : 'Failed to fetch recent solves'
        }
    }
}

/**
 * Get top solvers by number of challenges solved
 */
export async function getTopSolvers(limit: number = 10): Promise<ApiResult<LeaderboardEntry[]>> {
    try {
        const response = await apiClient.get('/api/solves/top-solvers', {
            params: { limit }
        })
        return {
            success: true,
            data: Object.entries(response.data).map(([username, solveCount]) => ({
                username,
                solveCount: solveCount as number
            }))
        }
    } catch (error) {
        console.error('Error fetching top solvers:', error)
        return {
            success: false,
            error: error instanceof Error ? error.message : 'Failed to fetch top solvers'
        }
    }
}

/**
 * Get most solved challenges
 */
export async function getMostSolvedChallenges(limit: number = 10): Promise<ApiResult<{ challengeId: string; solveCount: number }[]>> {
    try {
        const response = await apiClient.get('/api/solves/most-solved', {
            params: { limit }
        })
        return {
            success: true,
            data: Object.entries(response.data).map(([challengeId, solveCount]) => ({
                challengeId,
                solveCount: solveCount as number
            }))
        }
    } catch (error) {
        console.error('Error fetching most solved challenges:', error)
        return {
            success: false,
            error: error instanceof Error ? error.message : 'Failed to fetch most solved challenges'
        }
    }
}

/**
 * Get statistics for the authenticated user
 */
export async function getUserStatistics(): Promise<ApiResult<SolveStatistics>> {
    try {
        const response = await apiClient.get('/api/solves/me/stats')
        const data = response.data || {}

        // Validate that we have the required data
        if (!data || typeof data !== 'object') {
            console.error('Invalid user statistics data received:', data)
            return {
                success: false,
                error: 'Invalid data format received from server'
            }
        }

        return {
            success: true,
            data: {
                totalSolves: data.totalSolves || 0,
                totalPoints: data.totalPoints || 0,
                categoryDistribution: data.categoryDistribution || {},
                difficultyDistribution: data.difficultyDistribution || {},
                challengesSolved: data.totalSolves || 0 // Alias for consistency
            }
        }
    } catch (error) {
        console.error('Error fetching user statistics:', error)
        return {
            success: false,
            error: error instanceof Error ? error.message : 'Failed to fetch user statistics'
        }
    }
}

/**
 * Get statistics for a specific challenge
 */
export async function getChallengeStatistics(challengeId: string): Promise<ApiResult<ChallengeStatistics>> {
    try {
        const response = await apiClient.get(`/api/solves/challenge/${challengeId}/stats`)
        console.log('🔍 Raw API Response:', response)

        // The data is directly on response, not response.data
        const data = response || {}

        // Validate that we have the required data
        if (!data || typeof data !== 'object') {
            console.error('Invalid challenge statistics data received:', data)
            return {
                success: false,
                error: 'Invalid data format received from server'
            }
        }

        return {
            success: true,
            data: {
                challengeId: data.challengeId ?? '',
                challengeTitle: data.challengeTitle ?? '',
                solveCount: data.solveCount ?? 0,
                category: data.category ?? '',
                difficulty: data.difficulty ?? '',
                points: data.points ?? 0,
                solveRate: data.solveRate ?? 'N/A'
            }
        }
    } catch (error) {
        console.error('Error fetching challenge statistics:', error)
        return {
            success: false,
            error: error instanceof Error ? error.message : 'Failed to fetch challenge statistics'
        }
    }
}

/**
 * Get total number of solves in the system
 */
export async function getTotalSolveCount(): Promise<ApiResult<{ totalSolves: number }>> {
    try {
        const response = await apiClient.get('/api/solves/total-count')
        return {
            success: true,
            data: response.data
        }
    } catch (error) {
        console.error('Error fetching total solve count:', error)
        return {
            success: false,
            error: error instanceof Error ? error.message : 'Failed to fetch total solve count'
        }
    }
}