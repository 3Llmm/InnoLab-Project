import type { Metadata } from "next"
import CategoryPage from "@/components/category-page"

export const metadata: Metadata = {
  title: "Cryptography | CTF Platform",
  description: "Break ciphers, analyze encryption, and crack codes",
}

export default function CryptographyPage() {
  return (
    <CategoryPage
      category="cryptography"
      title="Cryptography"
      description="Break ciphers, analyze encryption algorithms, and crack cryptographic codes"
      color="from-blue-500 to-cyan-500"
    />
  )
}
