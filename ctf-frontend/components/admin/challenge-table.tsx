"use client"

import { useState } from "react"
import { Pencil, Trash2, Search } from "lucide-react"
import type { Challenge } from "@/lib/types"
import { deleteChallenge } from "@/lib/actions/admin"
import EditChallengeModal from "./edit-challenge-modal"

interface ChallengeTableProps {
  challenges: Challenge[]
}

export default function ChallengeTable({ challenges }: ChallengeTableProps) {
  const [searchTerm, setSearchTerm] = useState("")
  const [categoryFilter, setCategoryFilter] = useState<string>("all")
  const [difficultyFilter, setDifficultyFilter] = useState<string>("all")
  const [editingChallenge, setEditingChallenge] = useState<Challenge | null>(null)

  const filteredChallenges = challenges.filter((challenge) => {
    const matchesSearch = challenge.title.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesCategory = categoryFilter === "all" || challenge.category === categoryFilter
    const matchesDifficulty = difficultyFilter === "all" || challenge.difficulty === difficultyFilter
    return matchesSearch && matchesCategory && matchesDifficulty
  })

  const handleDelete = async (id: string) => {
    if (!confirm("Are you sure you want to delete this challenge?")) return

    try {
      // Get JWT token
      const token = localStorage.getItem('auth_token')
      if (!token) {
        throw new Error("Not authenticated")
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/challenges/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || `Failed to delete challenge: ${response.status}`)
      }

      // Call server action for revalidation
      const result = await deleteChallenge(id)
      
      if (result.success) {
        alert("Challenge deleted successfully")
        window.location.reload()
      } else {
        alert(result.error || "Failed to delete challenge")
      }
    } catch (error) {
      console.error("Error deleting challenge:", error)
      alert(error instanceof Error ? error.message : "Failed to delete challenge")
    }
  }

  // ... rest of the component remains the same
  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case "easy":
        return "text-accent"
      case "medium":
        return "text-secondary"
      case "hard":
        return "text-destructive"
      default:
        return "text-foreground"
    }
  }

  return (
    <div className="bg-card border border-border rounded-lg overflow-hidden">
      {/* Filters */}
      <div className="p-6 border-b border-border">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <input
              type="text"
              placeholder="Search challenges..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-background border border-border rounded-lg text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="px-4 py-2 bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="all">All Categories</option>
            <option value="binary-exploitation">Binary Exploitation</option>
            <option value="cryptography">Cryptography</option>
            <option value="forensics">Forensics</option>
            <option value="reverse-engineering">Reverse Engineering</option>
            <option value="web-exploitation">Web Exploitation</option>
          </select>
          <select
            value={difficultyFilter}
            onChange={(e) => setDifficultyFilter(e.target.value)}
            className="px-4 py-2 bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="all">All Difficulties</option>
            <option value="easy">Easy</option>
            <option value="medium">Medium</option>
            <option value="hard">Hard</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-muted">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                ID
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                Title
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                Category
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                Difficulty
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                Points
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-muted-foreground uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {filteredChallenges.map((challenge) => (
              <tr key={challenge.id} className="hover:bg-muted/50 transition-colors">
                <td className="px-6 py-4 whitespace-nowrap text-sm text-muted-foreground">{challenge.id}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-foreground">{challenge.title}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-foreground capitalize">
                  {challenge.category.replace("-", " ")}
                </td>
                <td
                  className={`px-6 py-4 whitespace-nowrap text-sm font-medium capitalize ${getDifficultyColor(challenge.difficulty)}`}
                >
                  {challenge.difficulty}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-foreground">{challenge.points}</td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <button
                    onClick={() => setEditingChallenge(challenge)}
                    className="text-primary hover:text-primary/80 mr-4 inline-flex items-center gap-1"
                  >
                    <Pencil className="w-4 h-4" />
                    Edit
                  </button>
                  <button
                    onClick={() => handleDelete(challenge.id)}
                    className="text-destructive hover:text-destructive/80 inline-flex items-center gap-1"
                  >
                    <Trash2 className="w-4 h-4" />
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredChallenges.length === 0 && (
        <div className="p-12 text-center">
          <p className="text-muted-foreground">No challenges found matching your filters.</p>
        </div>
      )}

      {editingChallenge && (
        <EditChallengeModal challenge={editingChallenge} onClose={() => setEditingChallenge(null)} />
      )}
    </div>
  )
}