import { apiClient } from './client';

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
        const backendStats = await apiClient.get('/api/challenges/admin/stats');

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
        };
    } catch (error) {
        console.error('Failed to fetch admin stats:', error);
        throw error;
    }
}

// Add the missing createChallenge function
export async function createChallenge(formData: FormData): Promise<any> {
    try {
        console.log('Sending create challenge request to http://localhost:8081/api/challenges...');

        // Log FormData contents for debugging
        for (const [key, value] of formData.entries()) {
            if (value instanceof File) {
                console.log(`FormData[${key}]: File "${value.name}" (${value.size} bytes, ${value.type})`);
            } else {
                console.log(`FormData[${key}]: ${value}`);
            }
        }

        const response = await fetch('http://localhost:8081/api/challenges', {
            method: 'POST',
            body: formData,
            credentials: 'include',
            // Note: Don't set Content-Type header for FormData - browser sets it automatically
        });

        console.log('Response received:', {
            status: response.status,
            statusText: response.statusText,
            headers: Object.fromEntries(response.headers.entries()),
            ok: response.ok,
            url: response.url
        });

        if (!response.ok) {
            // Try to read error response
            let errorText = '';
            try {
                errorText = await response.text();
                console.log('Error response text:', errorText);
            } catch (readError) {
                console.error('Could not read error response:', readError);
            }

            let errorMessage = `HTTP ${response.status} ${response.statusText}`;
            if (errorText) {
                // Try to parse as JSON
                try {
                    const errorData = JSON.parse(errorText);
                    errorMessage = errorData.message || errorData.error || errorMessage;
                } catch {
                    // Not JSON, use as text
                    errorMessage = errorText || errorMessage;
                }
            }

            console.error('Create challenge failed:', errorMessage);
            throw new Error(errorMessage);
        }

        // Parse successful response
        try {
            const result = await response.json();
            console.log('Create challenge successful:', result);
            return result;
        } catch (jsonError) {
            console.error('Failed to parse response as JSON:', jsonError);
            throw new Error('Server returned invalid JSON response');
        }

    } catch (error) {
        console.error('Network error creating challenge:', error);
        throw error;
    }
}

// You might also want to add other admin functions here:
export async function deleteChallenge(id: string): Promise<void> {
    return apiClient.delete(`/api/challenges/${id}`);
}

export async function updateChallenge(id: string, data: any): Promise<any> {
    return apiClient.put(`/api/challenges/${id}`, data);
}

export async function getAllUsers(): Promise<any[]> {
    return apiClient.get('/api/admin/users');
}