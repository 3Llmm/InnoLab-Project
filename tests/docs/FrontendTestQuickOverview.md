# Quick overview of the existing frontend tests

### Quick Start: 

npm i -D vitest @testing-library/react @testing-library/jest-dom jsdom @vitejs/plugin-react

npm test     # run all tests
npm run test:ui   # interactive UI

### Core Setup:

Environment: Vitest + jsdom
React Plugin: @vitejs/plugin-react (for automatic JSX runtime)
Global Setup: ctf-frontend/src/test/setup.ts 
    → import '@testing-library/jest-dom'
Alias: @ → project root (see vitest.config.ts)

All network calls mocked with vi.mock or fetch stubs

### Current Coverage:

| Area             | Purpose              | Key Checks                                             |
| ---------------- | -------------------- | ------------------------------------------------------ |
| **apiClient**    | HTTP wrapper         | adds Auth header if token exists; readable errors      |
| **useAuth Hook** | Auth state & storage | init state (from localStorage); login stores token/user; logout clears both |
| **Helpers**      | shared asserts       | `expectLoggedIn(result,{user,token})`, `expectLoggedOut(result)` |

### Where to find:

ctf-frontend...

* src/test/setup.ts,
* src/test/example.test.ts    # just for testing if vitest works
* lib/api/__tests__/client.test.ts,
* lib/hooks/__tests__/use-auth.test.tsx,
* vitest.config.ts

### Additional:

added .vitest/ and .vite/ to .gitignore
