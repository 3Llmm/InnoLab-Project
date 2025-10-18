"use server"

import { cookies } from "next/headers"
import { redirect } from "next/navigation"
import bcrypt from "bcryptjs"
import { SignJWT, jwtVerify } from "jose"
import type { AuthResult } from "@/lib/types"

// JWT secret key - In production, use environment variable
const SECRET_KEY = new TextEncoder().encode(process.env.JWT_SECRET || "your-secret-key-change-in-production")

// TODO: Replace with actual database calls
// This is a mock implementation for demonstration
const MOCK_USERS = new Map<string, any>()

export async function registerUser(formData: FormData): Promise<AuthResult> {
  try {
    const username = formData.get("username") as string
    const email = formData.get("email") as string
    const password = formData.get("password") as string

    // Validation
    if (!username || !email || !password) {
      return { success: false, error: "All fields are required" }
    }

    // Check if user already exists
    // TODO: Replace with database query
    if (MOCK_USERS.has(username) || Array.from(MOCK_USERS.values()).some((u) => u.email === email)) {
      return { success: false, error: "Username or email already exists" }
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10)

    // Create user
    // TODO: Replace with database insert
    const user = {
      id: crypto.randomUUID(),
      username,
      email,
      password: hashedPassword,
      score: 0,
      solvedChallenges: 0,
      createdAt: new Date(),
    }

    MOCK_USERS.set(username, user)

    return { success: true }
  } catch (error) {
    console.error("Registration error:", error)
    return { success: false, error: "Registration failed" }
  }
}

export async function loginUser(formData: FormData): Promise<AuthResult> {
  try {
    const username = formData.get("username") as string
    const password = formData.get("password") as string

    if (!username || !password) {
      return { success: false, error: "Username and password are required" }
    }

    // Find user
    // TODO: Replace with database query
    const user = MOCK_USERS.get(username) || Array.from(MOCK_USERS.values()).find((u) => u.email === username)

    if (!user) {
      return { success: false, error: "Invalid credentials" }
    }

    // Verify password
    const isValidPassword = await bcrypt.compare(password, user.password)
    if (!isValidPassword) {
      return { success: false, error: "Invalid credentials" }
    }

    // Create JWT token
    const token = await new SignJWT({ userId: user.id, username: user.username })
      .setProtectedHeader({ alg: "HS256" })
      .setExpirationTime("7d")
      .sign(SECRET_KEY)

    // Set cookie
    const cookieStore = await cookies()
    cookieStore.set("auth-token", token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 60 * 60 * 24 * 7, // 7 days
    })

    return {
      success: true,
      user: {
        id: user.id,
        username: user.username,
        email: user.email,
        score: user.score,
        solvedChallenges: user.solvedChallenges,
        createdAt: user.createdAt,
      },
    }
  } catch (error) {
    console.error("Login error:", error)
    return { success: false, error: "Login failed" }
  }
}

export async function logoutUser() {
  const cookieStore = await cookies()
  cookieStore.delete("auth-token")
  redirect("/")
}

export async function getCurrentUser() {
  try {
    const cookieStore = await cookies()
    const token = cookieStore.get("auth-token")?.value

    if (!token) {
      return null
    }

    const { payload } = await jwtVerify(token, SECRET_KEY)

    // TODO: Replace with database query
    const user = MOCK_USERS.get(payload.username as string)

    if (!user) {
      return null
    }

    return {
      id: user.id,
      username: user.username,
      email: user.email,
      score: user.score,
      solvedChallenges: user.solvedChallenges,
      createdAt: user.createdAt,
    }
  } catch (error) {
    return null
  }
}
