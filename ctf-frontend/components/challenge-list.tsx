"use client"

import { useState } from "react"
import Link from "next/link"
import { Search } from "lucide-react"
import type { Challenge } from "@/lib/types"

interface ChallengeListProps {
  challenges: Challenge[]
}

export default function ChallengeList({ challenges }: ChallengeListProps) {
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedCategory, setSelectedCategory] = useState<string>("all")
  const [selectedDifficulty, setSelectedDifficulty] = useState<string>("all")

  const categories = [
    "all",
    "binary-exploitation",
    "cryptography",
    "forensics",
    "reverse-engineering",
    "web-exploitation",
  ]
  const difficulties = ["all", "easy", "medium", "hard"]

  const filteredChallenges = challenges.filter((challenge) => {
    const matchesSearch =
      challenge.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      challenge.description.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesCategory = selectedCategory === "all" || challenge.category === selectedCategory
    const matchesDifficulty = selectedDifficulty === "all" || challenge.difficulty === selectedDifficulty

    return matchesSearch && matchesCategory && matchesDifficulty
  })

  return (
    <div>
      {/* Filters */}
      <div className="bg-card p-6 rounded-lg border border-border mb-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <input
              type="text"
              placeholder="Search challenges..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="px-4 py-2 bg-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
          >
            {categories.map((cat) => (
              <option key={cat} value={cat}>
                {cat === "all"
                  ? "All Categories"
                  : cat
                      .split("-")
                      .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
                      .join(" ")}
              </option>
            ))}
          </select>

          <select
            value={selectedDifficulty}
            onChange={(e) => setSelectedDifficulty(e.target.value)}
            className="px-4 py-2 bg-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
          >
            {difficulties.map((diff) => (
              <option key={diff} value={diff}>
                {diff === "all" ? "All Difficulties" : diff.charAt(0).toUpperCase() + diff.slice(1)}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Challenge Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredChallenges.map((challenge) => (
          <ChallengeCard key={challenge.id} challenge={challenge} />
        ))}
      </div>

      {filteredChallenges.length === 0 && (
        <div className="text-center py-12">
          <p className="text-muted-foreground">No challenges found matching your criteria</p>
        </div>
      )}
    </div>
  )
}

function ChallengeCard({ challenge }: { challenge: Challenge }) {
  const difficultyColors = {
    easy: "text-green-500",
    medium: "text-yellow-500",
    hard: "text-red-500",
  }

  return (
    <Link href={`/challenges/${challenge.id}`}>
      <div className="bg-card p-6 rounded-lg border border-border hover:border-primary transition-all hover:scale-105 h-full">
        <div className="flex justify-between items-start mb-3">
          <h3 className="text-xl font-semibold">{challenge.title}</h3>
          <span className={`text-sm font-semibold ${difficultyColors[challenge.difficulty]}`}>
            {challenge.difficulty.toUpperCase()}
          </span>
        </div>

        <p className="text-muted-foreground text-sm mb-4 line-clamp-2">{challenge.description}</p>

        <div className="flex justify-between items-center">
          <span className="text-xs px-3 py-1 bg-primary/10 text-primary rounded-full">
            {challenge.category
              .split("-")
              .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
              .join(" ")}
          </span>
          <span className="text-sm font-semibold text-accent">{challenge.points} pts</span>
        </div>

        {challenge.solved && (
          <div className="mt-4 pt-4 border-t border-border">
            <span className="text-xs text-accent font-semibold"> SOLVED</span>
          </div>
        )}
      </div>
    </Link>
  )
}
