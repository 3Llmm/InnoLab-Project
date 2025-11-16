// Core type definitions for the CTF platform

export interface User {
  id: string
  username: string
  email: string
  score: number
  solvedChallenges: number
  createdAt: Date
  isAdmin: boolean
}

export interface Challenge {
  id: string
  title: string
  description: string
  category: "binary-exploitation" | "cryptography" | "forensics" | "reverse-engineering" | "web-exploitation"
  difficulty: "easy" | "medium" | "hard"
  points: number
  solved: boolean  
  fileurl: string  
  flag?: string                                 
  hints?: string[]
  files?: { name: string; url: string }[]
}

export interface ScoreboardEntry {
  id: string
  username: string
  score: number
  solvedChallenges: number
}

export interface AuthResult {
  success: boolean
  error?: string
  user?: User
}

export interface FlagSubmissionResult {
  success: boolean
  message: string
  points?: number
}

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  status: string;
  message: string;
}

export interface AuthState {
  token: string | null;
  user: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean 
}

export interface ApiResult<T = any> {
  success: boolean;
  data?: T;
  error?: string;
}