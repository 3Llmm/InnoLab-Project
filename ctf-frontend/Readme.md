# CTF Training Platform - Next.js Frontend

A modern, full-stack Capture The Flag (CTF) training platform built with Next.js 15, TypeScript, and Tailwind CSS.

##  Features

- **Modern Tech Stack**: Next.js 15 App Router, TypeScript, Tailwind CSS v4
- **Authentication**: JWT-based auth with secure cookie storage
- **Challenge System**: Browse, filter, and solve CTF challenges across multiple categories
- **Scoreboard**: Competitive leaderboard tracking user progress
- **Responsive Design**: Mobile-first design with dark theme
- **Category Pages**: Dedicated pages for each CTF category
- **Course Structure**: Organized learning paths for systematic skill development

##  Project Structure

\`\`\`
ctf-frontend-next/
 app/                          # Next.js App Router pages
    layout.tsx               # Root layout with navbar/footer
    page.tsx                 # Homepage
    login/page.tsx           # Login page
    challenges/              # Challenge pages
       page.tsx            # Challenge list
       [id]/page.tsx       # Individual challenge
    scoreboard/page.tsx      # Leaderboard
    courses/page.tsx         # Course catalog
    about/page.tsx           # About page
    help/page.tsx            # Help center
    binary-exploitation/     # Category pages
    cryptography/
    forensics/
    reverse-engineering/
    web-exploitation/
    globals.css              # Global styles with Tailwind v4
 components/                   # React components
    navbar.tsx               # Navigation bar
    footer.tsx               # Footer
    login-form.tsx           # Login form
    challenge-list.tsx       # Challenge grid with filters
    challenge-detail.tsx     # Challenge detail view
    category-page.tsx        # Reusable category page
 lib/                         # Utilities and business logic
    types.ts                 # TypeScript type definitions
    actions/                 # Server Actions
       auth.ts             # Authentication actions
       challenges.ts       # Challenge submission
    api/                     # API client functions
       challenges.ts       # Challenge data fetching
       scoreboard.ts       # Scoreboard data
    hooks/                   # Custom React hooks
        use-auth.ts         # Authentication hook
 public/                      # Static assets
    images/                 # Image files
 package.json                 # Dependencies
 tsconfig.json               # TypeScript config
 next.config.js              # Next.js config
 README.md                   # This file
\`\`\`

##  Installation

1. **Clone the repository**
   \`\`\`bash
   git clone <repository-url>
   cd ctf-frontend-next
   \`\`\`

2. **Install dependencies**
   \`\`\`bash
   npm install
   \`\`\`

3. **Set up environment variables**
   Create a `.env.local` file:
   \`\`\`env
   JWT_SECRET=your-secret-key-change-in-production
   NEXT_PUBLIC_API_URL=http://localhost:3000/api
   \`\`\`

4. **Run the development server**
   \`\`\`bash
   npm run dev
   \`\`\`

5. **Open your browser**
   Navigate to [http://localhost:3000](http://localhost:3000)

##  Configuration

### Tailwind CSS v4
Tailwind configuration is done in `app/globals.css` using the new `@theme` directive. Customize colors, fonts, and other design tokens there.

### Authentication
Authentication is handled via Server Actions in `lib/actions/auth.ts`. Currently uses in-memory storage for demo purposes. **Replace with database integration for production.**

### API Integration
Mock data is used in `lib/api/` files. Replace these with actual API calls to your backend:

\`\`\`typescript
// Example: Replace mock data with real API calls
export async function getAllChallenges(): Promise<Challenge[]> {
  const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/challenges`)
  return response.json()
}
\`\`\`

##  Backend Integration Checklist

- [ ] Set up database (PostgreSQL, MySQL, MongoDB, etc.)
- [ ] Create user authentication endpoints
- [ ] Implement challenge CRUD operations
- [ ] Add flag validation logic
- [ ] Create scoreboard calculation
- [ ] Set up file storage for challenge files
- [ ] Implement rate limiting for flag submissions
- [ ] Add user profile management
- [ ] Create admin panel for challenge management

##  Key Files to Customize

1. **`lib/actions/auth.ts`** - Replace mock user storage with database
2. **`lib/api/challenges.ts`** - Connect to your challenge API
3. **`lib/api/scoreboard.ts`** - Connect to your scoreboard API
4. **`app/globals.css`** - Customize theme colors and design tokens
5. **`lib/types.ts`** - Add/modify types as your data structure evolves

##  Design System

The platform uses a dark theme with cyber-inspired colors:
- **Primary**: Cyan (`#00d9ff`) - Main brand color
- **Secondary**: Purple (`#7c3aed`) - Accent color
- **Accent**: Green (`#10b981`) - Success states
- **Background**: Dark blue (`#0a0e27`)
- **Card**: Lighter dark (`#1a1f3a`)

##  Deployment

### Vercel (Recommended)
\`\`\`bash
npm run build
vercel deploy
\`\`\`

### Other Platforms
\`\`\`bash
npm run build
npm start
\`\`\`

##  Technologies Used

- **Next.js 15** - React framework with App Router
- **TypeScript** - Type safety
- **Tailwind CSS v4** - Utility-first CSS
- **bcryptjs** - Password hashing
- **jose** - JWT token handling
- **Lucide React** - Icon library

##  Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

##  License

This project is licensed under the MIT License.

##  Support

For help and support:
- Check the `/help` page in the app
- Open an issue on GitHub
- Contact: support@ctfplatform.com

---

**Note**: This is a frontend-only implementation with mock data. You'll need to integrate it with a backend API for full functionality.
