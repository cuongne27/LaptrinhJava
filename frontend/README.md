# EVM Frontend - Electric Vehicle Management System

Frontend application for EVM system built with Next.js 14, React, TypeScript, and Tailwind CSS.

## Features

- 🎨 Modern UI with Tailwind CSS and shadcn/ui components
- 📱 Fully responsive design
- 🔐 JWT-based authentication
- 📊 Dashboard with charts and analytics
- 🛠️ Complete CRUD operations for all entities
- 📈 Reports and statistics
- 🔍 Advanced filtering and search
- 🎯 Role-based access control

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Build

```bash
npm run build
npm start
```

## Project Structure

```
frontend/
├── app/                    # Next.js app directory
│   ├── (auth)/            # Authentication pages
│   ├── (dashboard)/       # Dashboard and main pages
│   └── layout.tsx         # Root layout
├── components/            # Reusable components
│   ├── ui/               # shadcn/ui components
│   ├── layout/           # Layout components
│   └── features/         # Feature-specific components
├── lib/                  # Utilities and helpers
│   ├── api/             # API client
│   ├── hooks/           # Custom hooks
│   └── utils/           # Utility functions
├── types/               # TypeScript types
├── store/              # State management (Zustand)
└── public/             # Static assets
```

## Tech Stack

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **UI Components**: shadcn/ui + Radix UI
- **State Management**: Zustand
- **Data Fetching**: TanStack Query
- **Forms**: React Hook Form + Zod
- **Charts**: Recharts
- **Icons**: Lucide React

## Environment Variables

Create a `.env.local` file:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## License

MIT

