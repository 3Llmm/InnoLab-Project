"use client"

import type React from "react"
import { useState } from "react"
import { X } from "lucide-react"
import type { Challenge } from "@/lib/types"
import { updateChallenge } from "@/lib/actions/admin"

interface EditChallengeModalProps {
  challenge: Challenge
  onClose: () => void
}

export default function EditChallengeModal({ challenge, onClose }: EditChallengeModalProps) {
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setIsSubmitting(true)

    try {
      const formData = new FormData(e.currentTarget)
      const title = formData.get("title") as string
      const description = formData.get("description") as string
      const category = formData.get("category") as string
      const difficulty = formData.get("difficulty") as string
      const points = Number.parseInt(formData.get("points") as string)
      const flag = formData.get("flag") as string
      const file = formData.get("file") as File | null

      // Create multipart form data for the backend
      const multipartFormData = new FormData()
      multipartFormData.append('title', title)
      multipartFormData.append('description', description)
      multipartFormData.append('category', category)
      multipartFormData.append('difficulty', difficulty)
      multipartFormData.append('points', points.toString())
      multipartFormData.append('flag', flag)
      
      if (file && file.size > 0) {
        multipartFormData.append('file', file)
      }

      // CHANGED: Remove manual token, use credentials instead
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/challenges/${challenge.id}`, {
        method: 'PUT',
        credentials: 'include', // Send cookies automatically
        body: multipartFormData,
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || `Failed to update challenge: ${response.status}`)
      }

      // Call server action for revalidation
      const result = await updateChallenge(challenge.id, new FormData())
      
      if (result.success) {
        alert("Challenge updated successfully!")
        onClose()
        window.location.reload()
      } else {
        alert(result.error || "Failed to update challenge")
      }
    } catch (error) {
      console.error("Error updating challenge:", error)
      alert(error instanceof Error ? error.message : "Failed to update challenge")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <div className="bg-card border border-border rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-card border-b border-border p-6 flex items-center justify-between">
          <h2 className="text-2xl font-bold text-foreground">Edit Challenge</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-muted rounded-lg transition-colors"
            aria-label="Close modal"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="edit-title" className="block text-sm font-medium text-foreground mb-2">
                Title *
              </label>
              <input
                type="text"
                id="edit-title"
                name="title"
                required
                defaultValue={challenge.title}
                className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label htmlFor="edit-points" className="block text-sm font-medium text-foreground mb-2">
                Points *
              </label>
              <input
                type="number"
                id="edit-points"
                name="points"
                required
                min="0"
                defaultValue={challenge.points}
                className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>
          </div>

          <div>
            <label htmlFor="edit-description" className="block text-sm font-medium text-foreground mb-2">
              Description *
            </label>
            <textarea
              id="edit-description"
              name="description"
              required
              rows={4}
              defaultValue={challenge.description}
              className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary resize-none"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="edit-category" className="block text-sm font-medium text-foreground mb-2">
                Category *
              </label>
              <select
                id="edit-category"
                name="category"
                required
                defaultValue={challenge.category}
                className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
              >
                <option value="binary-exploitation">Binary Exploitation</option>
                <option value="cryptography">Cryptography</option>
                <option value="forensics">Forensics</option>
                <option value="reverse-engineering">Reverse Engineering</option>
                <option value="web-exploitation">Web Exploitation</option>
              </select>
            </div>

            <div>
              <label htmlFor="edit-difficulty" className="block text-sm font-medium text-foreground mb-2">
                Difficulty *
              </label>
              <select
                id="edit-difficulty"
                name="difficulty"
                required
                defaultValue={challenge.difficulty}
                className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
              >
                <option value="easy">Easy</option>
                <option value="medium">Medium</option>
                <option value="hard">Hard</option>
              </select>
            </div>
          </div>


          <div>
            <label htmlFor="edit-file" className="block text-sm font-medium text-foreground mb-2">
              Challenge File (optional - leave empty to keep current file)
            </label>
            <input
              type="file"
              id="edit-file"
              name="file"
              className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:bg-primary file:text-primary-foreground file:cursor-pointer hover:file:bg-primary/90"
            />
            <p className="text-xs text-muted-foreground mt-2">
              Current file: {challenge.fileurl ? 'Uploaded' : 'None'}
            </p>
          </div>

          <div className="flex gap-4">
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 py-3 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSubmitting ? "Updating..." : "Update Challenge"}
            </button>
            <button
              type="button"
              onClick={onClose}
              className="px-6 py-3 bg-muted text-foreground rounded-lg hover:bg-muted/80 transition-colors font-medium"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}