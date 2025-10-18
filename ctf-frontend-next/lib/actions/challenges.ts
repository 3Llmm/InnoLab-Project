"use server"

import { getCurrentUser } from "./auth"
import type { FlagSubmissionResult } from "@/lib/types"

export async function submitFlag(challengeId: string, flag: string): Promise<FlagSubmissionResult> {
  try {
    const user = await getCurrentUser()

    if (!user) {
      return { success: false, message: "You must be logged in to submit flags" }
    }

    // TODO: Replace with actual API call to backend
    // This is a mock implementation

    // Validate flag format
    if (!flag.startsWith("flag{") || !flag.endsWith("}")) {
      return { success: false, message: "Invalid flag format. Flags should be in the format: flag{...}" }
    }

    // Mock flag validation - In production, verify against database
    const isCorrect = flag === `flag{mock_flag_for_challenge_${challengeId}}`

    if (isCorrect) {
      // TODO: Update user score and challenge status in database
      return {
        success: true,
        message: "Congratulations! Flag accepted! 🎉",
        points: 100, // Mock points
      }
    } else {
      return {
        success: false,
        message: "Incorrect flag. Try again!",
      }
    }
  } catch (error) {
    console.error("Flag submission error:", error)
    return {
      success: false,
      message: "An error occurred while submitting the flag",
    }
  }
}
