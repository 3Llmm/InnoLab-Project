import type { Metadata } from "next"
import ChallengeList from "@/components/challenge-list"
import { getAllChallenges } from "@/lib/api/challenges"

export const metadata: Metadata = {
  title: "Challenges | CTF Platform",
  description: "Browse and solve CTF challenges",
}

export default async function ChallengesPage() {
  // TODO: Replace with actual API call when backend is ready
  const challenges = await getAllChallenges()

  return (
    <div className="min-h-screen py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-4xl font-bold mb-2">Challenges</h1>
          <p className="text-muted-foreground">Test your skills across multiple categories</p>
        </div>

        <ChallengeList challenges={challenges} />
      </div>  
    </div>
  )
}
