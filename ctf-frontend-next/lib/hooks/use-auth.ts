"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import type { User } from "@/lib/types"
import { logoutUser } from "@/lib/actions/auth"

export function useAuth() {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const router = useRouter()

  useEffect(() => {
    // TODO: Fetch current user from API
    // For now, check if auth cookie exists
    const checkAuth = async () => {
      try {
        // This would be replaced with an actual API call
        // const response = await fetch('/api/auth/me')
        // const data = await response.json()
        // setUser(data.user)
        setLoading(false)
      } catch (error) {
        setLoading(false)
      }
    }

    checkAuth()
  }, [])

  const logout = async () => {
    await logoutUser()
    setUser(null)
    router.push("/")
  }

  return { user, loading, logout }
}
