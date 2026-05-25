import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useAuth, AuthProvider } from '../../hooks/use-auth'
import React from "react";

const ORIGINAL_FETCH = global.fetch
const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

function expectLoggedOut(result: any) {
    const { auth } = result.current
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.user).toBeNull()
    expect(auth.token).toBeNull()
}

function expectLoggedIn(result: any, { user }: { user: string }) {
    const { auth } = result.current
    expect(auth.isAuthenticated).toBe(true)
    expect(auth.user).toBe(user)
}


function wrapper({ children }: { children: React.ReactNode }) {
    return <AuthProvider>{children}</AuthProvider>
}

beforeEach(() => {
    localStorage.clear()
    global.fetch = vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({}),
        headers: {
            get: (name: string) => name === 'Content-Type' ? 'application/json' : null,
        },
    } as any)
})

afterEach(() => {
    localStorage.clear()
    global.fetch = ORIGINAL_FETCH
    vi.clearAllMocks()
    Object.defineProperty(document, 'cookie', {
        writable: true,
        value: '',
    })
})

describe('useAuth basic flow', () => {
    it('Start: no Token = unauthenticated', () => {
        const { result } = renderHook(() => useAuth(), { wrapper })

        expectLoggedOut(result)
    })

    it('login() saves token and sets status to authenticated', async () => {
        ;(global.fetch as any).mockResolvedValueOnce({
            ok: true,
            status: 200,
            headers: {
                get: (name: string) => name === 'Content-Type' ? 'application/json' : null,
            },
            json: async () => ({ status: 'success', username: 'Alice', isAdmin: false }),
        })

        const { result } = renderHook(() => useAuth(), { wrapper })

        await act(async () => {
            await result.current.login?.({ username: 'Alice', password: 'secret' } as any)
        })

        await waitFor(() => {
            expectLoggedIn(result, { user: 'Alice' })
        })
    })

    it('logout() deletes token and sets status back', async () => {
        ;(global.fetch as any).mockResolvedValueOnce({
            ok: true,
            status: 200,
            headers: {
                get: (name: string) => name === 'Content-Type' ? 'application/json' : null,
            },
            json: async () => ({}),
        })

        const { result } = renderHook(() => useAuth(), { wrapper })
        
        await act(async () => {
            await result.current.logout?.()
        })

        await waitFor(() => {
            expectLoggedOut(result)
        })
    })

    it('login() throws understandable error if not ok', async () => {
        ;(global.fetch as any)
            .mockResolvedValueOnce({
                ok: false,
                status: 401,
                headers: {
                    get: (name: string) => name === 'Content-Type' ? 'application/json' : null,
                },
                json: async () => ({ message: 'Login failed' }),
            })
            .mockResolvedValueOnce({
                ok: true,
                status: 200,
                headers: {
                    get: (name: string) => name === 'Content-Type' ? 'application/json' : null,
                },
                json: async () => ({}),
            })

        const { result } = renderHook(() => useAuth(), { wrapper })

        let resp: any
        await act(async () => {
            resp = await result.current.login?.({ username: 'Alice', password: 'wrong' } as any)
        })

        expect(resp).toMatchObject({
            success: false,
            error: expect.stringMatching(/Login failed/i),
        })

        await waitFor(() => {
            expectLoggedOut(result)
        })
    })
})
