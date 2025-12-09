import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { apiClient } from '../../api/client'

const ORIGINAL_FETCH = global.fetch
const TOKEN_KEY = 'auth_token'

function headersFrom(init: any) {
    // init.headers could be header or plain object
    // thats why we make an object out of it
    if (!init?.headers) return {}
    if (typeof (init.headers as any).get === 'function') {
        const obj: Record<string, string> = {}
        ;(init.headers as any).forEach((value: string, key: string) => (obj[key] = value))
        return obj
    }
    return init.headers as Record<string, string>
}

beforeEach(() => {
    localStorage.clear()
    global.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({ ok: true }),
        status: 200,
    } as any)
})

afterEach(() => {
    global.fetch = ORIGINAL_FETCH
    localStorage.clear()
})

describe('apiClient', () => {
    it('sets auth header if token is present', async () => {
        localStorage.setItem(TOKEN_KEY, 'TEST_TOKEN')

        await apiClient.get('/api/ping')

        expect(global.fetch).toHaveBeenCalledTimes(1)
        const [, init] = (global.fetch as any).mock.calls[0]
        const h = headersFrom(init)

        expect(h['Authorization'] || h['authorization']).toBe('Bearer TEST_TOKEN')
    })

    it('throws understandable error if not ok', async () => {
        ;(global.fetch as any).mockResolvedValueOnce({
            ok: false,
            status: 401,
            json: async () => ({ message: 'Unauthorized' }),
        })

        await expect(apiClient.get('/api/ping')).rejects.toThrow(/Unauthorized/i)
    })

    it('does not set auth header without token', async () => {
        await apiClient.get('/api/ping')
        const [, init] = (global.fetch as any).mock.calls[0]
        const h = headersFrom(init)

        expect(h['Authorization'] || h['authorization']).toBeUndefined()
    })
})
