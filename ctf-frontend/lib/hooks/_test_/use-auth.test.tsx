import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useAuth, AuthProvider } from '../../hooks/use-auth'
import React from "react";

const ORIGINAL_FETCH = global.fetch
const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

function expectLoggedOut(result: any) {
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull()
    expect(localStorage.getItem(USER_KEY)).toBeNull()
    const { auth } = result.current
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.user).toBeNull()
    expect(auth.token).toBeNull()
}

function expectLoggedIn(result: any, { user, token }: { user: string; token: string }) {
    expect(localStorage.getItem(TOKEN_KEY)).toBe(token)
    expect(localStorage.getItem(USER_KEY)).toBe(user)
    const { auth } = result.current
    expect(auth.isAuthenticated).toBe(true)
    expect(auth.user).toBe(user)
    expect(auth.token).toBe(token)
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
    } as any)
})

afterEach(() => {
    localStorage.clear()
    global.fetch = ORIGINAL_FETCH
    vi.clearAllMocks()
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
            json: async () => ({ token: 'TEST_TOKEN', user: { id: 'u1', name: 'Alice' } }),
        })

        const { result } = renderHook(() => useAuth(), { wrapper })

        await act(async () => {
            await result.current.login?.({ username: 'Alice', password: 'secret' } as any)
        })

        await waitFor(() => {
            expectLoggedIn(result, { user: 'Alice', token: 'TEST_TOKEN' })
        })
    })

    it('logout() deletes token and sets status back', async () => {
        localStorage.setItem(TOKEN_KEY, 'TEST_TOKEN')
        localStorage.setItem(USER_KEY,'Alice')

        const { result } = renderHook(() => useAuth(), { wrapper })
        act(() => {
            result.current.logout?.()
        })

        expectLoggedOut(result)
    })

    it('login() throws understandable error if not ok', async () => {
        ;(global.fetch as any).mockResolvedValueOnce({
            ok: false,
            status: 401,
            json: async () => ({ message: 'Login failed' }),
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
