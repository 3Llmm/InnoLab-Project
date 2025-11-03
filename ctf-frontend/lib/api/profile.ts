// Mock user profile data - replace with API calls to your backend
export interface UserProfile {
  username: string
  email: string
  memberSince: string
  rank: string
  totalPoints: number
  challengesSolved: number
  streak: number
  timeSpent: string
  recentSubmissions: Array<{
    challenge: string
    category: string
    solved: boolean
    time: string
  }>
}

export function getMockProfile(): UserProfile {
  // TODO: Replace with actual API call to fetch user profile
  // const response = await fetch(`/api/profile`);
  // return response.json();

  return {
    username: "CyberNinja",
    email: "cyberninja@ctf-platform.com",
    memberSince: "January 2024",
    rank: "Elite Hacker",
    totalPoints: 8750,
    challengesSolved: 42,
    streak: 15,
    timeSpent: "127 hours",
    recentSubmissions: [
      {
        challenge: "Buffer Overflow Basics",
        category: "Binary Exploitation",
        solved: true,
        time: "2 hours ago",
      },
      {
        challenge: "Caesar Cipher",
        category: "Cryptography",
        solved: true,
        time: "1 day ago",
      },
      {
        challenge: "Memory Forensics",
        category: "Forensics",
        solved: true,
        time: "2 days ago",
      },
      {
        challenge: "SQL Injection Defense",
        category: "Web Exploitation",
        solved: false,
        time: "3 days ago",
      },
    ],
  }
}
