import type { Metadata } from "next"
import CategoryPage from "@/components/category-page"

export const metadata: Metadata = {
  title: "Forensics | CTF Platform",
  description: "Investigate digital evidence and recover hidden data",
}

export default function ForensicsPage() {
  return (
    <CategoryPage
      category="forensics"
      title="Digital Forensics"
      description="Investigate digital evidence, analyze file systems, and recover hidden data"
      color="from-green-500 to-emerald-500"
    />
  )
}
