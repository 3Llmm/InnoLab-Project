'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/lib/hooks/use-auth'
import { getChallengeById } from '@/lib/api/challenges'
import ChallengeDetail from '@/components/challenge-detail'
import type { Challenge } from '@/lib/types'
import { Loader2, AlertCircle, ArrowLeft } from 'lucide-react'
import Link from 'next/link'

export default function ChallengePage({ params }: { params: Promise<{ id: string }> }) { 
  const { auth, isLoading: authLoading } = useAuth()
  const router = useRouter()
  const [challenge, setChallenge] = useState<Challenge | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [resolvedId, setResolvedId] = useState<string | null>(null) 

 
  useEffect(() => {
    const resolveParams = async () => {
      const resolved = await params
      setResolvedId(resolved.id)
    }
    resolveParams()
  }, [params])

  // Redirect if not authenticated
  useEffect(() => {
    if (!authLoading && !auth.isAuthenticated) {
      router.push('/login')
    }
  }, [auth.isAuthenticated, authLoading, router])

  // Fetch challenge - now using resolvedId
  useEffect(() => {
    if (auth.isAuthenticated && resolvedId) {
      setLoading(true)
      getChallengeById(resolvedId)
        .then(setChallenge)
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false))
    }
  }, [auth.isAuthenticated, resolvedId]) 

  // Show loading state
  if (authLoading || loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="w-12 h-12 animate-spin mx-auto mb-4 text-primary" />
          <p className="text-muted-foreground">Loading challenge...</p>
        </div>
      </div>
    )
  }

  // Show error state
  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center px-4">
        <div className="max-w-md w-full">
          <div className="p-6 bg-destructive/10 border border-destructive rounded-lg">
            <div className="flex items-start gap-3">
              <AlertCircle className="w-6 h-6 text-destructive flex-shrink-0 mt-0.5" />
              <div>
                <h3 className="font-semibold text-destructive mb-1">Error Loading Challenge</h3>
                <p className="text-sm text-destructive/90">{error}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  // Challenge not found
  if (!challenge) {
    return (
      <div className="min-h-screen flex items-center justify-center px-4">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-2">Challenge Not Found</h2>
          <p className="text-muted-foreground mb-4">
            The challenge you're looking for doesn't exist.
          </p>
          <Link
            href="/challenges"
            className="inline-flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:opacity-90"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Challenges
          </Link>
        </div>
      </div>
    )
  }

  // Don't render if not authenticated
  if (!auth.isAuthenticated) {
    return null
  }

  return (
    <div className="min-h-screen py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <ChallengeDetail challenge={challenge} />
      </div>
    </div>
  )
}