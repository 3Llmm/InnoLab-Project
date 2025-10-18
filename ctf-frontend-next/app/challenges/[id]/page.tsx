import type { Metadata } from "next"
import { notFound } from "next/navigation"
import ChallengeDetail from "@/components/challenge-detail"
import { getChallengeById } from "@/lib/api/challenges"

interface ChallengePageProps {
  params: Promise<{ id: string }>
}

export async function generateMetadata({ params }: ChallengePageProps): Promise<Metadata> {
  const { id } = await params
  const challenge = await getChallengeById(id)

  if (!challenge) {
    return {
      title: "Challenge Not Found | CTF Platform",
    }
  }

  return {
    title: `${challenge.title} | CTF Platform`,
    description: challenge.description,
  }
}

export default async function ChallengePage({ params }: ChallengePageProps) {
  const { id } = await params
  const challenge = await getChallengeById(id)

  if (!challenge) {
    notFound()
  }

  return (
    <div className="min-h-screen py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <ChallengeDetail challenge={challenge} />
      </div>
    </div>
  )
}
