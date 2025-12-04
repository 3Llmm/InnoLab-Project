import type React from "react"
import Link from "next/link"
import { Shield, Target, Trophy, Users } from "lucide-react"

export default function HomePage() {
  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative py-20 px-4 sm:px-6 lg:px-8 bg-gradient-to-br from-background via-card to-background">
        <div className="max-w-7xl mx-auto text-center">
<h1 className="text-5xl md:text-7xl font-bold mb-6 bg-clip-text text-transparent
  bg-gradient-to-r from-blue-700 via-purple-700 to-pink-600
  dark:from-blue-400 dark:via-purple-400 dark:to-pink-300 leading-tight pb-1">
  CTF Training Platform
</h1>

          <p className="text-xl md:text-2xl text-muted-foreground mb-8 max-w-3xl mx-auto">
            Master cybersecurity skills through hands-on Capture The Flag challenges
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              href="/challenges"
              className="px-8 py-4 bg-primary text-primary-foreground rounded-lg font-semibold hover:opacity-90 transition-opacity"
            >
              Start Challenges
            </Link>
            <Link
              href="/courses"
              className="px-8 py-4 bg-secondary text-secondary-foreground rounded-lg font-semibold hover:opacity-90 transition-opacity"
            >
              Browse Courses
            </Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto">
          <h2 className="text-4xl font-bold text-center mb-12">Why Train With Us?</h2>
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            <FeatureCard
              icon={<Shield className="w-12 h-12 text-primary" />}
              title="Real-World Skills"
              description="Practice with challenges based on actual security scenarios"
            />
            <FeatureCard
              icon={<Target className="w-12 h-12 text-primary" />}
              title="Multiple Categories"
              description="Binary exploitation, cryptography, forensics, and more"
            />
            <FeatureCard
              icon={<Trophy className="w-12 h-12 text-primary" />}
              title="Competitive Scoring"
              description="Track your progress and compete on the leaderboard"
            />
            <FeatureCard
              icon={<Users className="w-12 h-12 text-primary" />}
              title="Community Driven"
              description="Learn from others and share your knowledge"
            />
          </div>
        </div>
      </section>

      {/* Categories Section */}
      <section className="py-20 px-4 sm:px-6 lg:px-8 bg-card">
        <div className="max-w-7xl mx-auto">
          <h2 className="text-4xl font-bold text-center mb-12">Challenge Categories</h2>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            <CategoryCard
              title="Binary Exploitation"
              description="Master buffer overflows, ROP chains, and memory corruption"
              href="/binary-exploitation"
              color="from-red-500 to-orange-500"
            />
            <CategoryCard
              title="Cryptography"
              description="Break ciphers, analyze encryption, and crack codes"
              href="/cryptography"
              color="from-blue-500 to-cyan-500"
            />
            <CategoryCard
              title="Forensics"
              description="Investigate digital evidence and recover hidden data"
              href="/forensics"
              color="from-green-500 to-emerald-500"
            />
            <CategoryCard
              title="Reverse Engineering"
              description="Analyze binaries, understand malware, and decompile code"
              href="/reverse-engineering"
              color="from-purple-500 to-pink-500"
            />
            <CategoryCard
              title="Web Exploitation"
              description="Find and exploit vulnerabilities in web applications"
              href="/web-exploitation"
              color="from-yellow-500 to-orange-500"
            />
            <CategoryCard
              title="All Challenges"
              description="Browse all available challenges across categories"
              href="/challenges"
              color="from-primary to-secondary"
            />
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-4xl font-bold mb-6">Ready to Start Your Journey?</h2>
          <p className="text-xl text-muted-foreground mb-8">
            Join thousands of security enthusiasts learning and competing
          </p>
          <Link
            href="/login"
            className="inline-block px-8 py-4 bg-accent text-accent-foreground rounded-lg font-semibold hover:opacity-90 transition-opacity"
          >
            Login with your FH credentials
          </Link>
        </div>
      </section>
    </div>
  )
}

function FeatureCard({ icon, title, description }: { icon: React.ReactNode; title: string; description: string }) {
  return (
    <div className="p-6 bg-card rounded-lg border border-border hover:border-primary transition-colors">
      <div className="mb-4">{icon}</div>
      <h3 className="text-xl font-semibold mb-2">{title}</h3>
      <p className="text-muted-foreground">{description}</p>
    </div>
  )
}

function CategoryCard({
  title,
  description,
  href,
  color,
}: { title: string; description: string; href: string; color: string }) {
  return (
    <Link href={href} className="group">
      <div className="p-6 bg-card rounded-lg border border-border hover:border-primary transition-all hover:scale-105">
        <div className={`w-full h-2 rounded-full bg-gradient-to-r ${color} mb-4`} />
        <h3 className="text-xl font-semibold mb-2 group-hover:text-primary transition-colors">{title}</h3>
        <p className="text-muted-foreground">{description}</p>
      </div>
    </Link>
  )
}
