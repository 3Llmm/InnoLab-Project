import type { Metadata } from "next"
import { MessageCircle, BookOpen, Shield, Mail } from "lucide-react"

export const metadata: Metadata = {
  title: "Help | CTF Platform",
  description: "Get help and support for the CTF platform",
}

export default function HelpPage() {
  return (
    <div className="min-h-screen py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-6 text-center">Help Center</h1>

        <div className="grid md:grid-cols-2 gap-6 mb-12">
          <div className="bg-card p-6 rounded-lg border border-border hover:border-primary transition-colors">
            <MessageCircle className="w-12 h-12 text-primary mb-4" />
            <h2 className="text-xl font-semibold mb-2">Community Forum</h2>
            <p className="text-muted-foreground mb-4">Connect with other users, ask questions, and share knowledge</p>
            <a href="#forum" className="text-primary hover:underline font-medium">
              Visit Forum →
            </a>
          </div>

          <div className="bg-card p-6 rounded-lg border border-border hover:border-primary transition-colors">
            <BookOpen className="w-12 h-12 text-secondary mb-4" />
            <h2 className="text-xl font-semibold mb-2">Documentation</h2>
            <p className="text-muted-foreground mb-4">Comprehensive guides and tutorials to get you started</p>
            <a href="#docs" className="text-primary hover:underline font-medium">
              Read Docs →
            </a>
          </div>

          <div className="bg-card p-6 rounded-lg border border-border hover:border-primary transition-colors">
            <Shield className="w-12 h-12 text-accent mb-4" />
            <h2 className="text-xl font-semibold mb-2">Rules & Guidelines</h2>
            <p className="text-muted-foreground mb-4">Platform rules, code of conduct, and competition guidelines</p>
            <a href="#rules" className="text-primary hover:underline font-medium">
              View Rules →
            </a>
          </div>

          <div className="bg-card p-6 rounded-lg border border-border hover:border-primary transition-colors">
            <Mail className="w-12 h-12 text-primary mb-4" />
            <h2 className="text-xl font-semibold mb-2">Contact Support</h2>
            <p className="text-muted-foreground mb-4">Need help? Our support team is here to assist you</p>
            <a href="#contact" className="text-primary hover:underline font-medium">
              Contact Us →
            </a>
          </div>
        </div>

        <div id="faq" className="bg-card p-8 rounded-lg border border-border mb-8">
          <h2 className="text-2xl font-semibold mb-6">Frequently Asked Questions</h2>

          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold mb-2">How do I get started?</h3>
              <p className="text-muted-foreground">
                Create a free account, browse the challenges page, and start with easier challenges to familiarize
                yourself with the platform. We recommend starting with the courses section for structured learning
                paths.
              </p>
            </div>

            <div>
              <h3 className="text-lg font-semibold mb-2">What is a CTF flag?</h3>
              <p className="text-muted-foreground">
                A flag is a specific string of text that proves you&apos;ve successfully completed a challenge. Flags
                typically follow the format{" "}
                <code className="px-2 py-1 bg-muted rounded font-mono text-sm">
                  flag{"{"}...{"}"}
                </code>
                . Submit the flag to earn points and mark the challenge as solved.
              </p>
            </div>

            <div>
              <h3 className="text-lg font-semibold mb-2">How does scoring work?</h3>
              <p className="text-muted-foreground">
                Each challenge has a point value based on its difficulty. When you successfully submit a flag, you earn
                those points. Your total score determines your position on the leaderboard.
              </p>
            </div>

            <div>
              <h3 className="text-lg font-semibold mb-2">Can I get hints?</h3>
              <p className="text-muted-foreground">
                Many challenges include hints that can guide you in the right direction. Hints are available on the
                challenge detail page. We encourage you to try solving challenges independently first, but hints are
                there when you need them.
              </p>
            </div>

            <div id="forgot-password">
              <h3 className="text-lg font-semibold mb-2">I forgot my password. What should I do?</h3>
              <p className="text-muted-foreground">
                Click the “Forgot password?” link on the login page. Enter your email address, and we&apos;ll send you
                instructions to reset your password.
              </p>
            </div>

            <div id="rules">
              <h3 className="text-lg font-semibold mb-2">What are the platform rules?</h3>
              <p className="text-muted-foreground">
                • No sharing of flags or solutions publicly
                <br />• No attacking the platform infrastructure
                <br />• No automated flag submission (brute forcing)
                <br />• Respect other users and maintain a positive community
                <br />• Report any bugs or vulnerabilities responsibly
              </p>
            </div>
          </div>
        </div>

        <div id="contact" className="bg-card p-8 rounded-lg border border-border">
          <h2 className="text-2xl font-semibold mb-4">Contact Us</h2>
          <p className="text-muted-foreground mb-6">
            Have a question that&apos;s not answered here? Need technical support? We&apos;re here to help!
          </p>
          <div className="space-y-3">
            <div className="flex items-center gap-3">
              <Mail className="w-5 h-5 text-primary" />
              <a href="mailto:support@technikum-wien.at" className="text-primary hover:underline">
                support@technikum-wien.at
              </a>
            </div>
            <p className="text-sm text-muted-foreground">We typically respond within 24 hours during business days.</p>
          </div>
        </div>
      </div>
    </div>
  )
}
