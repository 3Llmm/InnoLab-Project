"use client"

import type React from "react"
import { useState } from "react"
import Link from "next/link"
import { ArrowLeft, Flag, Download, AlertCircle, CheckCircle2 } from "lucide-react"
import { apiClient } from "@/lib/api/client"
import type { Challenge } from "@/lib/types"

interface ChallengeDetailProps {
  challenge: Challenge
}

export default function ChallengeDetail({ challenge }: ChallengeDetailProps) {
  const [flag, setFlag] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState<{ success: boolean; message: string } | null>(null)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    setResult(null)

    try {
      // Call your REST endpoint
      const response = await apiClient.post<{ correct: boolean; message: string }>(
        "/api/flags/submit",
        { challengeId: challenge.id, flag }
      )

      // Normalize to { success, message } for the UI
      const next = {
        success: Boolean(response?.correct),
        message: response?.message ?? "",
      }

      setResult(next)
      if (next.success) setFlag("")
    } catch (error) {
      setResult({
        success: false,
        message: error instanceof Error ? error.message : "An error occurred",
      })
    } finally {
      setSubmitting(false)
    }
  }

  const difficultyColors = {
    easy: "text-green-500 bg-green-500/10",
    medium: "text-yellow-500 bg-yellow-500/10",
    hard: "text-red-500 bg-red-500/10",
  } as const

  return (
    <div>
      <Link
        href="/challenges"
        className="inline-flex items-center gap-2 text-muted-foreground hover:text-primary transition-colors mb-6"
      >
        <ArrowLeft className="w-4 h-4" />
        Back to Challenges
      </Link>

      <div className="bg-card p-8 rounded-lg border border-border">
        <div className="flex flex-wrap items-start justify-between gap-4 mb-6">
          <div>
            <h1 className="text-3xl font-bold mb-2">{challenge.title}</h1>
            <div className="flex flex-wrap gap-3">
              <span
                className={`text-sm font-semibold px-3 py-1 rounded-full ${difficultyColors[challenge.difficulty]}`}
              >
                {challenge.difficulty.toUpperCase()}
              </span>
              <span className="text-sm px-3 py-1 bg-primary/10 text-primary rounded-full">
                {challenge.category
                  .split("-")
                  .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
                  .join(" ")}
              </span>
            </div>
          </div>
          <div className="text-right">
            <div className="text-3xl font-bold text-accent">{challenge.points}</div>
            <div className="text-sm text-muted-foreground">points</div>
          </div>
        </div>

        <div className="prose prose-invert max-w-none mb-8">
          <h2 className="text-xl font-semibold mb-3">Description</h2>
          <p className="text-muted-foreground">{challenge.description}</p>

          {challenge.hints && challenge.hints.length > 0 && (
            <div className="mt-6">
              <h3 className="text-lg font-semibold mb-2">Hints</h3>
              <ul className="space-y-2">
                {challenge.hints.map((hint, index) => (
                  <li key={index} className="text-muted-foreground">
                    {hint}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {challenge.files && challenge.files.length > 0 && (
            <div className="mt-6">
              <h3 className="text-lg font-semibold mb-3">Files</h3>
              <div className="space-y-2">
                {challenge.files.map((file, index) => (
                  <a
                    key={index}
                    href={file.url}
                    download
                    className="flex items-center gap-2 px-4 py-2 bg-background border border-border rounded-lg hover:border-primary transition-colors"
                  >
                    <Download className="w-4 h-4" />
                    <span>{file.name}</span>
                  </a>
                ))}
              </div>
            </div>
          )}
        </div>

        {!challenge.solved && (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="flag" className="block text-sm font-medium mb-2">
                Submit Flag
              </label>
              <div className="flex gap-3">
                <input
                  type="text"
                  id="flag"
                  value={flag}
                  onChange={(e) => setFlag(e.target.value)}
                  placeholder="flag{...}"
                  className="flex-1 px-4 py-2 bg-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary font-mono"
                  required
                />
                <button
                  type="submit"
                  disabled={submitting || !flag}
                  className="px-6 py-2 bg-primary text-primary-foreground rounded-lg font-semibold hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                  <Flag className="w-4 h-4" />
                  {submitting ? "Submitting..." : "Submit"}
                </button>
              </div>
            </div>

            {result && (
              <div
                className={`p-4 rounded-lg flex items-start gap-3 ${
                  result.success
                    ? "bg-accent/10 border border-accent"
                    : "bg-destructive/10 border border-destructive"
                }`}
              >
                {result.success ? (
                  <CheckCircle2 className="w-5 h-5 text-accent flex-shrink-0 mt-0.5" />
                ) : (
                  <AlertCircle className="w-5 h-5 text-destructive flex-shrink-0 mt-0.5" />
                )}
                <p className={`text-sm ${result.success ? "text-accent" : "text-destructive"}`}>
                  {result.message}
                </p>
              </div>
            )}
          </form>
        )}

        {challenge.solved && (
          <div className="p-4 bg-accent/10 border border-accent rounded-lg flex items-center gap-3">
            <CheckCircle2 className="w-5 h-5 text-accent" />
            <p className="text-accent font-semibold">Challenge Solved! 🎉</p>
          </div>
        )}
      </div>
    </div>
  )
}
