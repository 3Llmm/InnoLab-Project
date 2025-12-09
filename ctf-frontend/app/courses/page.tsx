import type { Metadata } from "next"
import Link from "next/link"
import { Clock, Target } from "lucide-react"

export const metadata: Metadata = {
  title: "Courses | CTF Platform",
  description: "Learn cybersecurity through structured courses",
}

export default function CoursesPage() {
  const courses = [
    {
      id: "binary-exploitation",
      title: "Binary Exploitation",
      description: "Learn to exploit memory corruption vulnerabilities, buffer overflows, and ROP chains",
      href: "/binary-exploitation",
      duration: "8 weeks",
      challenges: 15,
      color: "from-red-500 to-orange-500",
    },
    {
      id: "cryptography",
      title: "Cryptography",
      description: "Master classical and modern cryptography, from Caesar ciphers to RSA",
      href: "/cryptography",
      duration: "6 weeks",
      challenges: 12,
      color: "from-blue-500 to-cyan-500",
    },
    {
      id: "forensics",
      title: "Digital Forensics",
      description: "Investigate digital evidence, analyze file systems, and recover hidden data",
      href: "/forensics",
      duration: "7 weeks",
      challenges: 14,
      color: "from-green-500 to-emerald-500",
    },
    {
      id: "reverse-engineering",
      title: "Reverse Engineering",
      description: "Analyze binaries, understand assembly, and reverse engineer software",
      href: "/reverse-engineering",
      duration: "10 weeks",
      challenges: 18,
      color: "from-purple-500 to-pink-500",
    },
    {
      id: "web-exploitation",
      title: "Web Exploitation",
      description: "Discover and exploit web vulnerabilities like SQL injection and XSS",
      href: "/web-exploitation",
      duration: "8 weeks",
      challenges: 16,
      color: "from-yellow-500 to-orange-500",
    },
  ]

  return (
    <div className="min-h-screen py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold mb-2">Courses</h1>
          <p className="text-muted-foreground">Structured learning paths to master cybersecurity</p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {courses.map((course) => (
            <Link key={course.id} href={course.href} className="group">
              <div className="bg-card p-6 rounded-lg border border-border hover:border-primary transition-all hover:scale-105 h-full flex flex-col">
                <div className={`w-full h-2 rounded-full bg-gradient-to-r ${course.color} mb-4`} />

                <h3 className="text-xl font-semibold mb-3 group-hover:text-primary transition-colors">
                  {course.title}
                </h3>

                <p className="text-muted-foreground text-sm mb-6 flex-1">{course.description}</p>

                <div className="flex items-center justify-between text-sm text-muted-foreground pt-4 border-t border-border">
                  <div className="flex items-center gap-2">
                    <Clock className="w-4 h-4" />
                    <span>{course.duration}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Target className="w-4 h-4" />
                    <span>{course.challenges} challenges</span>
                  </div>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}
