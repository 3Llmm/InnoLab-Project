import type { Metadata } from "next"
import { Trophy, Medal, Award } from "lucide-react"
import { getScoreboard } from "@/lib/api/scoreboard"

export const metadata: Metadata = {
  title: "Scoreboard | CTF Platform",
  description: "View the CTF competition leaderboard",
}

export default async function ScoreboardPage() {
  // TODO: Replace with actual API call when backend is ready
  const scoreboard = await getScoreboard()

  return (
    <div className="min-h-screen py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold mb-2">Scoreboard</h1>
          <p className="text-muted-foreground">Top performers on the platform</p>
        </div>

        {/* Top 3 Podium */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          {scoreboard.slice(0, 3).map((user, index) => (
            <div
              key={user.id}
              className={`bg-card p-6 rounded-lg border-2 ${
                index === 0
                  ? "border-yellow-500 md:order-2"
                  : index === 1
                    ? "border-gray-400 md:order-1"
                    : "border-orange-600 md:order-3"
              }`}
            >
              <div className="text-center">
                <div className="mb-4">
                  {index === 0 && <Trophy className="w-16 h-16 mx-auto text-yellow-500" />}
                  {index === 1 && <Medal className="w-16 h-16 mx-auto text-gray-400" />}
                  {index === 2 && <Award className="w-16 h-16 mx-auto text-orange-600" />}
                </div>
                <div className="text-3xl font-bold mb-2">#{index + 1}</div>
                <div className="text-xl font-semibold mb-1">{user.username}</div>
                <div className="text-2xl font-bold text-primary">{user.score} pts</div>
                <div className="text-sm text-muted-foreground mt-2">{user.solvedChallenges} challenges solved</div>
              </div>
            </div>
          ))}
        </div>

        {/* Full Leaderboard */}
        <div className="bg-card rounded-lg border border-border overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-muted">
                <tr>
                  <th className="px-6 py-4 text-left text-sm font-semibold">Rank</th>
                  <th className="px-6 py-4 text-left text-sm font-semibold">Username</th>
                  <th className="px-6 py-4 text-right text-sm font-semibold">Score</th>
                  <th className="px-6 py-4 text-right text-sm font-semibold">Solved</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {scoreboard.map((user, index) => (
                  <tr key={user.id} className="hover:bg-muted/50 transition-colors">
                    <td className="px-6 py-4 text-sm font-medium">
                      {index < 3 ? (
                        <span className="text-primary">#{index + 1}</span>
                      ) : (
                        <span className="text-muted-foreground">#{index + 1}</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm font-medium">{user.username}</td>
                    <td className="px-6 py-4 text-sm font-bold text-primary text-right">{user.score}</td>
                    <td className="px-6 py-4 text-sm text-muted-foreground text-right">{user.solvedChallenges}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}
