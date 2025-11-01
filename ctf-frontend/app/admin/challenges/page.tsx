"use client"

import type { Metadata } from "next"
import Link from "next/link"
import { ArrowLeft } from "lucide-react"
import { useEffect, useState } from "react"
import { getAllChallenges } from "@/lib/api/challenges" // Use client version
import ChallengeTable from "@/components/admin/challenge-table"
import AddChallengeForm from "@/components/admin/add-challenge-form"
import type { Challenge } from "@/lib/types"

// Remove metadata export since client components can't export metadata
// You can use next/headers instead or keep it in layout

export default function AdminChallengesPage() {
  const [challenges, setChallenges] = useState<Challenge[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const loadChallenges = async () => {
      try {
        const data = await getAllChallenges()
        setChallenges(data)
      } catch (error) {
        console.error('Failed to load challenges:', error)
        setChallenges([])
      } finally {
        setIsLoading(false)
      }
    }

    loadChallenges()
  }, [])

  if (isLoading) {
    return (
      <div className="min-h-screen bg-background py-12 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="flex items-center justify-center">
            <p className="text-foreground">Loading challenges...</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background py-12 px-4">
      <div className="max-w-7xl mx-auto">
        <div className="flex items-center gap-4 mb-8">
          <Link
            href="/admin"
            className="p-2 hover:bg-muted rounded-lg transition-colors"
            aria-label="Back to dashboard"
          >
            <ArrowLeft className="w-5 h-5" />
          </Link>
          <div>
            <h1 className="text-4xl font-bold text-foreground mb-2">Manage Challenges</h1>
            <p className="text-muted-foreground">Create, edit, and delete CTF challenges</p>
          </div>
        </div>

        {/* Add Challenge Form */}
        <div className="mb-12">
          <AddChallengeForm />
        </div>

        {/* Challenge Table */}
        <ChallengeTable challenges={challenges} />
      </div>
    </div>
  )
}