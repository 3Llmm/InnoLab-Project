import type { Metadata } from "next"
import RegisterForm from "@/components/register-form"

export const metadata: Metadata = {
  title: "Register | CTF Platform",
  description: "Create your CTF training account",
}

export default function RegisterPage() {
  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold mb-2">Join the Platform</h1>
          <p className="text-muted-foreground">Create your account and start learning</p>
        </div>
        <RegisterForm />
      </div>
    </div>
  )
}
