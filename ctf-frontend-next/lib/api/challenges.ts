// API functions for fetching challenge data
// TODO: Replace mock data with actual API calls to your backend

import type { Challenge } from "@/lib/types"

// Mock challenge data
const MOCK_CHALLENGES: Challenge[] = [
  {
    id: "1",
    title: "Buffer Overflow Basics",
    description: "Learn the fundamentals of buffer overflow exploitation. Can you overwrite the return address?",
    category: "binary-exploitation",
    difficulty: "easy",
    points: 100,
    solved: false,
    hints: ["Look at the buffer size", "What happens when you write past the buffer?"],
    files: [{ name: "vuln.c", url: "/files/vuln.c" }],
  },
  {
    id: "2",
    title: "Caesar Cipher",
    description: "A classic substitution cipher. Can you decrypt the message?",
    category: "cryptography",
    difficulty: "easy",
    points: 50,
    solved: false,
    hints: ["Try all possible shifts", "The shift is less than 26"],
  },
  {
    id: "3",
    title: "Hidden in Plain Sight",
    description: "There's more to this image than meets the eye. Can you find the hidden data?",
    category: "forensics",
    difficulty: "medium",
    points: 150,
    solved: false,
    files: [{ name: "image.png", url: "/files/image.png" }],
  },
  {
    id: "4",
    title: "Crackme Challenge",
    description: "Reverse engineer this binary to find the correct password.",
    category: "reverse-engineering",
    difficulty: "medium",
    points: 200,
    solved: false,
    files: [{ name: "crackme", url: "/files/crackme" }],
  },
  {
    id: "5",
    title: "SQL Injection 101",
    description: "Exploit this vulnerable login form to gain admin access.",
    category: "web-exploitation",
    difficulty: "easy",
    points: 100,
    solved: false,
    hints: ["Try common SQL injection payloads", "Think about bypassing authentication"],
  },
]

export async function getAllChallenges(): Promise<Challenge[]> {
  // TODO: Replace with actual API call
  // Example: const response = await fetch('/api/challenges')
  // return response.json()

  return MOCK_CHALLENGES
}

export async function getChallengeById(id: string): Promise<Challenge | null> {
  // TODO: Replace with actual API call
  // Example: const response = await fetch(`/api/challenges/${id}`)
  // return response.json()

  return MOCK_CHALLENGES.find((c) => c.id === id) || null
}

export async function getChallengesByCategory(category: string): Promise<Challenge[]> {
  // TODO: Replace with actual API call
  // Example: const response = await fetch(`/api/challenges?category=${category}`)
  // return response.json()

  return MOCK_CHALLENGES.filter((c) => c.category === category)
}
