"use server"

import { revalidatePath } from "next/cache"

interface ActionResult {
  success: boolean
  error?: string
}

// Note: Since we need to include JWT token from localStorage (client-side),
// we'll handle the actual API calls in client components
// These server actions will just coordinate the revalidation

export async function createChallenge(formData: FormData): Promise<ActionResult> {
  try {
    // In a real implementation, you might want to handle the API call here
    // But since we need the JWT token from localStorage, we'll handle it client-side
    // This server action is mainly for revalidation
    
    console.log(" Creating challenge via server action")
    
    // Revalidate the challenges page
    revalidatePath("/admin/challenges")
    revalidatePath("/challenges")

    return { success: true }
  } catch (error) {
    console.error(" Error in createChallenge:", error)
    return {
      success: false,
      error: error instanceof Error ? error.message : "Failed to create challenge",
    }
  }
}

export async function updateChallenge(id: string, formData: FormData): Promise<ActionResult> {
  try {
    console.log(" Updating challenge via server action:", id)
    
    // Revalidate the challenges page
    revalidatePath("/admin/challenges")
    revalidatePath("/challenges")
    revalidatePath(`/challenges/${id}`)

    return { success: true }
  } catch (error) {
    console.error(" Error updating challenge:", error)
    return {
      success: false,
      error: error instanceof Error ? error.message : "Failed to update challenge",
    }
  }
}

export async function deleteChallenge(id: string): Promise<ActionResult> {
  try {
    console.log(" Deleting challenge via server action:", id)

    // Revalidate the challenges page
    revalidatePath("/admin/challenges")
    revalidatePath("/challenges")

    return { success: true }
  } catch (error) {
    console.error(" Error deleting challenge:", error)
    return {
      success: false,
      error: error instanceof Error ? error.message : "Failed to delete challenge",
    }
  }
}