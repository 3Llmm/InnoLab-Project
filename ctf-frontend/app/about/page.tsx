import type { Metadata } from "next"
import { Shield, Users, Target, Zap } from "lucide-react"

export const metadata: Metadata = {
  title: "About | CTF Platform",
  description: "Learn about our CTF training platform",
}

export default function AboutPage() {
  return (
    <div className="min-h-screen py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-6 text-center">About CTF Platform</h1>

        <div className="bg-card p-8 rounded-lg border border-border mb-8">
          <p className="text-lg text-muted-foreground mb-6">
            CTF Platform is a comprehensive training environment designed to help cybersecurity enthusiasts and
            professionals develop their skills through hands-on Capture The Flag challenges.
          </p>

          <p className="text-muted-foreground mb-6">
            Our platform offers a wide range of challenges across multiple categories including binary exploitation,
            cryptography, forensics, reverse engineering, and web exploitation. Whether you're a beginner just starting
            your cybersecurity journey or an experienced professional looking to sharpen your skills, we have challenges
            suited to your level.
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-6 mb-8">
          <div className="bg-card p-6 rounded-lg border border-border">
            <Shield className="w-12 h-12 text-primary mb-4" />
            <h2 className="text-xl font-semibold mb-2">Our Mission</h2>
            <p className="text-muted-foreground">
              To provide accessible, high-quality cybersecurity training that prepares individuals for real-world
              security challenges.
            </p>
          </div>

          <div className="bg-card p-6 rounded-lg border border-border">
            <Users className="w-12 h-12 text-secondary mb-4" />
            <h2 className="text-xl font-semibold mb-2">Community</h2>
            <p className="text-muted-foreground">
              Join a supportive space where learners and experts come together to discuss challenges, share insights, and help each other grow in cybersecurity.
            </p>
          </div>

          <div className="bg-card p-6 rounded-lg border border-border">
            <Target className="w-12 h-12 text-accent mb-4" />
            <h2 className="text-xl font-semibold mb-2">Real-World Skills</h2>
            <p className="text-muted-foreground">
              Our challenges are based on actual security scenarios, ensuring you develop practical skills applicable in
              the field.
            </p>
          </div>

          <div className="bg-card p-6 rounded-lg border border-border">
            <Zap className="w-12 h-12 text-primary mb-4" />
            <h2 className="text-xl font-semibold mb-2">Continuous Learning</h2>
            <p className="text-muted-foreground">
              New challenges are added regularly, keeping content fresh and aligned with the latest security trends and
              techniques.
            </p>
          </div>
        </div>

        <div className="bg-card p-8 rounded-lg border border-border">
          <h2 className="text-2xl font-semibold mb-4">What Makes Us Different</h2>
          <ul className="space-y-3 text-muted-foreground">
            <li className="flex items-start gap-3">
              <span className="text-primary mt-1">•</span>
              <span>Structured learning paths that guide you from beginner to advanced levels</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-primary mt-1">•</span>
              <span>Detailed writeups to help you learn from each challenge</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-primary mt-1">•</span>
              <span>Competitive scoring system with leaderboards to track your progress</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-primary mt-1">•</span>
              <span>Regular CTF competitions and events to test your skills</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-primary mt-1">•</span>
              <span>Active community forums for discussion and collaboration</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  )
}
