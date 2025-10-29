import Link from "next/link"
import { ArrowLeft, BookOpen } from "lucide-react"
import ChallengeList from "@/components/challenge-list"
import { getChallengesByCategory } from "@/lib/api/challenges"

interface CategoryPageProps {
  category: string
  title: string
  description: string
  color: string
}

export default async function CategoryPage({ category, title, description, color }: CategoryPageProps) {
  // TODO: Replace with actual API call when backend is ready
  const challenges = await getChallengesByCategory(category)

  return (
    <div className="min-h-screen py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <Link
          href="/courses"
          className="inline-flex items-center gap-2 text-muted-foreground hover:text-primary transition-colors mb-6"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Courses
        </Link>

        <div className="mb-8">
          <div className={`w-full h-2 rounded-full bg-gradient-to-r ${color} mb-6`} />
          <h1 className="text-4xl font-bold mb-2">{title}</h1>
          <p className="text-muted-foreground text-lg">{description}</p>
        </div>

        <div className="grid lg:grid-cols-4 gap-8">
          <div className="lg:col-span-1">
            <div className="bg-card p-6 rounded-lg border border-border sticky top-24">
              <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
                <BookOpen className="w-5 h-5" />
                Course Info
              </h2>
              <div className="space-y-4 text-sm">
                <div>
                  <div className="text-muted-foreground mb-1">Total Challenges</div>
                  <div className="text-2xl font-bold text-primary">{challenges.length}</div>
                </div>
                <div>
                  <div className="text-muted-foreground mb-1">Difficulty Range</div>
                  <div className="font-semibold">Easy to Hard</div>
                </div>
                <div>
                  <div className="text-muted-foreground mb-1">Total Points</div>
                  <div className="font-semibold">{challenges.reduce((sum, c) => sum + c.points, 0)} pts</div>
                </div>
              </div>
            </div>
          </div>

          <div className="lg:col-span-3">
            <ChallengeList challenges={challenges} />
          </div>
        </div>
      </div>
    </div>
  )
}
