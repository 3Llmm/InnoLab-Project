import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export function middleware(request: NextRequest) {
  const authToken = request.cookies.get('auth_token')?.value
  
  // Protect routes that require authentication
  if (!authToken && request.nextUrl.pathname.startsWith('/challenges')) {
    return NextResponse.redirect(new URL('/login', request.url))
  }

  if (!authToken && request.nextUrl.pathname.startsWith('/profile')) {
    return NextResponse.redirect(new URL('/login', request.url))
  }

  if (!authToken && request.nextUrl.pathname.startsWith('/admin')) {
    return NextResponse.redirect(new URL('/login', request.url))
  }

  // Redirect authenticated users away from login
  if (authToken && request.nextUrl.pathname === '/login') {
    return NextResponse.redirect(new URL('/challenges', request.url))
  }

  return NextResponse.next()
}

export const config = {
  matcher: [
    '/challenges/:path*',
    '/profile/:path*',
    '/admin/:path*',
    '/login'
  ]
}