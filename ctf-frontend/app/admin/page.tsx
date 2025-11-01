"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { Shield, Users, Flag, TrendingUp } from "lucide-react"
import { getAdminStats, type AdminStats } from "@/lib/api/admin"

export default function AdminDashboardPage() {
  const [stats, setStats] = useState<AdminStats | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const loadStats = async () => {
      try {
        const data = await getAdminStats()
        setStats(data)
      } catch (err) {
        console.error('Failed to load admin stats:', err)
        setError(err instanceof Error ? err.message : 'Failed to load dashboard')
      } finally {
        setIsLoading(false)
      }
    }

    loadStats()
  }, [])

  if (isLoading) {
    return (
      <div className="min-h-screen bg-background py-12 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="flex items-center justify-center">
            <p className="text-foreground">Loading admin dashboard...</p>
          </div>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-background py-12 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-destructive mb-4">Error</h1>
            <p className="text-muted-foreground mb-4">{error}</p>
            <Link
              href="/login"
              className="px-6 py-3 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors font-medium"
            >
              Go to Login
            </Link>
          </div>
        </div>
      </div>
    )
  }

  if (!stats) {
    return (
      <div className="min-h-screen bg-background py-12 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="flex items-center justify-center">
            <p className="text-destructive">Failed to load admin dashboard</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background py-12 px-4">
      <div className="max-w-7xl mx-auto">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-4xl font-bold text-foreground mb-2">Admin Dashboard</h1>
            <p className="text-muted-foreground">Manage challenges, users, and platform statistics</p>
          </div>
          <Link
            href="/admin/challenges"
            className="px-6 py-3 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors font-medium"
          >
            Manage Challenges
          </Link>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
          <StatCard
            title="Total Challenges"
            value={stats.totalChallenges}
            icon={<Flag className="w-6 h-6" />}
            color="primary"
          />
          <StatCard
            title="Total Users"
            value={stats.totalUsers}
            icon={<Users className="w-6 h-6" />}
            color="secondary"
          />
          <StatCard
            title="Total Submissions"
            value={stats.totalSubmissions}
            icon={<TrendingUp className="w-6 h-6" />}
            color="accent"
          />
          <StatCard
            title="Active Challenges"
            value={stats.activeChallenges}
            icon={<Shield className="w-6 h-6" />}
            color="primary"
          />
        </div>

        {/* Category Breakdown */}
        <div className="bg-card border border-border rounded-lg p-6 mb-8">
          <h2 className="text-2xl font-bold text-foreground mb-6">Challenges by Category</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
            {stats.challengesByCategory.map((cat) => (
              <div key={cat.category} className="bg-muted rounded-lg p-4">
                <p className="text-sm text-muted-foreground mb-1 capitalize">{cat.category.replace("-", " ")}</p>
                <p className="text-3xl font-bold text-foreground">{cat.count}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Difficulty Breakdown */}
        <div className="bg-card border border-border rounded-lg p-6">
          <h2 className="text-2xl font-bold text-foreground mb-6">Challenges by Difficulty</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {stats.challengesByDifficulty.map((diff) => (
              <div key={diff.difficulty} className="bg-muted rounded-lg p-4">
                <p className="text-sm text-muted-foreground mb-1 capitalize">{diff.difficulty}</p>
                <p className="text-3xl font-bold text-foreground">{diff.count}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

function StatCard({
  title,
  value,
  icon,
  color,
}: {
  title: string
  value: number | string
  icon: React.ReactNode
  color: "primary" | "secondary" | "accent"
}) {
  const colorClasses = {
    primary: "bg-primary/10 text-primary",
    secondary: "bg-secondary/10 text-secondary",
    accent: "bg-accent/10 text-accent",
  }

  return (
    <div className="bg-card border border-border rounded-lg p-6 hover:border-primary/50 transition-colors">
      <div className="flex items-center justify-between mb-4">
        <div className={`p-3 rounded-lg ${colorClasses[color]}`}>{icon}</div>
      </div>
      <p className="text-sm text-muted-foreground mb-1">{title}</p>
      <p className="text-3xl font-bold text-foreground">{value}</p>
    </div>
  )
}