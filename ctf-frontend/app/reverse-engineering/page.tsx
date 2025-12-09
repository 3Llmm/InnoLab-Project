import type { Metadata } from "next"
import CategoryPage from "@/components/category-page"

export const metadata: Metadata = {
  title: "Reverse Engineering | CTF Platform",
  description: "Analyze binaries, understand malware, and decompile code",
}

export default function ReverseEngineeringPage() {
  return (
    <CategoryPage
      category="reverse-engineering"
      title="Reverse Engineering"
      description="Analyze binaries, understand assembly code, and reverse engineer software"
      color="from-purple-500 to-pink-500"
    />
  )
}
