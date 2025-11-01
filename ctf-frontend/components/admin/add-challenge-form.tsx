"use client"

import type React from "react"
import { useState } from "react"
import { Plus, X } from "lucide-react"
import { createChallenge } from "@/lib/actions/admin"

export default function AddChallengeForm() {
  const [isOpen, setIsOpen] = useState(false)
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
      } else {
        // If no file provided, create an empty file to avoid backend errors
        const emptyBlob = new Blob([], { type: 'application/octet-stream' })
        multipartFormData.append('file', emptyBlob, 'empty.txt')
      }

      // Get JWT token
      const token = localStorage.getItem('auth_token')
      if (!token) {
        throw new Error("Not authenticated")
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/challenges`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
        body: multipartFormData,
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || `Failed to create challenge: ${response.status}`)
      }

      // Call server action for revalidation
      const result = await createChallenge(new FormData())
      
      if (result.success) {
        alert("Challenge created successfully!")
        setIsOpen(false)
        window.location.reload()
      } else {
        alert(result.error || "Failed to create challenge")
      }
    } catch (error) {
      console.error("Error creating challenge:", error)
      alert(error instanceof Error ? error.message : "Failed to create challenge")
    } finally {
      setIsSubmitting(false)
    }
  }

  if (!isOpen) {
    return (
      <button
        onClick={() => setIsOpen(true)}
        className="w-full py-4 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors font-medium flex items-center justify-center gap-2"
      >
        <Plus className="w-5 h-5" />
        Add New Challenge
      </button>
    )
  }

  return (
    <div className="bg-card border border-border rounded-lg p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-foreground">Add New Challenge</h2>
        <button
          onClick={() => setIsOpen(false)}
          className="p-2 hover:bg-muted rounded-lg transition-colors"
          aria-label="Close form"
        >
          <X className="w-5 h-5" />
        </button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-foreground mb-2">
              Title *
            </label>
            <input
              type="text"
              id="title"
              name="title"
              required
              className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="Challenge title"
            />
          </div>

          <div>
            <label htmlFor="points" className="block text-sm font-medium text-foreground mb-2">
              Points *
            </label>
            <input
              type="number"
              id="points"
              name="points"
              required
              min="0"
              className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="100"
            />
          </div>
        </div>

        <div>
          <label htmlFor="description" className="block text-sm font-medium text-foreground mb-2">
            Description *
          </label>
          <textarea
            id="description"
            name="description"
            required
            rows={4}
            className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary resize-none"
            placeholder="Challenge description..."
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label htmlFor="category" className="block text-sm font-medium text-foreground mb-2">
              Category *
            </label>
            <select
              id="category"
              name="category"
              required
              className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            >
              <option value="">Select category</option>
              <option value="binary-exploitation">Binary Exploitation</option>
              <option value="cryptography">Cryptography</option>
              <option value="forensics">Forensics</option>
              <option value="reverse-engineering">Reverse Engineering</option>
              <option value="web-exploitation">Web Exploitation</option>
            </select>
          </div>

          <div>
            <label htmlFor="difficulty" className="block text-sm font-medium text-foreground mb-2">
              Difficulty *
            </label>
            <select
              id="difficulty"
              name="difficulty"
              required
              className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            >
              <option value="">Select difficulty</option>
              <option value="easy">Easy</option>
              <option value="medium">Medium</option>
              <option value="hard">Hard</option>
            </select>
          </div>
        </div>

        <div>
          <label htmlFor="flag" className="block text-sm font-medium text-foreground mb-2">
            Flag *
          </label>
          <input
            type="text"
            id="flag"
            name="flag"
            required
            className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            placeholder="CTF{example_flag}"
          />
        </div>

        <div>
          <label htmlFor="file" className="block text-sm font-medium text-foreground mb-2">
            Challenge File (optional)
          </label>
          <input
            type="file"
            id="file"
            name="file"
            className="w-full px-4 py-2 bg-background border border-border rounded-lg text-foreground file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:bg-primary file:text-primary-foreground file:cursor-pointer hover:file:bg-primary/90"
          />
          <p className="text-xs text-muted-foreground mt-2">Upload challenge files, binaries, or resources</p>
        </div>

        <div className="flex gap-4">
          <button
            type="submit"
            disabled={isSubmitting}
            className="flex-1 py-3 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isSubmitting ? "Creating..." : "Create Challenge"}
          </button>
          <button
            type="button"
            onClick={() => setIsOpen(false)}
            className="px-6 py-3 bg-muted text-foreground rounded-lg hover:bg-muted/80 transition-colors font-medium"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}