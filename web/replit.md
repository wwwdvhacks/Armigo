# Armenia Explorer - Chat & Trip Planner

## Overview

Armenia Explorer is a full-stack web application that combines Firebase-based real-time chat functionality with an interactive trip planning system focused on exploring Armenia. The application integrates with an existing Android app through shared Firebase infrastructure while providing a comprehensive web experience for trip planning through Armenia's regions and cities.

## System Architecture

### Frontend Architecture
- **Framework**: React 18 with TypeScript
- **Routing**: Wouter for client-side routing
- **UI Framework**: Shadcn/ui components with Radix UI primitives
- **Styling**: Tailwind CSS with custom Armenia-themed color scheme
- **State Management**: React hooks with custom state managers
- **Data Fetching**: TanStack Query (React Query)
- **Forms**: React Hook Form with Zod validation
- **Maps**: Leaflet.js for interactive Armenia map visualization

### Backend Architecture
- **Runtime**: Node.js with Express server
- **Build Tool**: Vite for development and production builds
- **Database ORM**: Drizzle ORM configured for PostgreSQL
- **Authentication**: Firebase Authentication (email/password)
- **Real-time Data**: Firebase Realtime Database
- **Session Management**: Express sessions with PostgreSQL storage

### Data Storage Solutions
- **Primary Database**: PostgreSQL with Drizzle ORM
- **Real-time Database**: Firebase Realtime Database for chat and trips
- **Authentication**: Firebase Auth with custom user profiles
- **Session Storage**: PostgreSQL-backed session store

## Key Components

### Authentication System
- Firebase Authentication for user registration/login
- Custom user profile storage in Firebase Realtime Database
- Username uniqueness validation
- Session management with automatic redirects

### Chat System
- Real-time messaging using Firebase Realtime Database
- Chat ID generation using sorted user IDs (`uid1_uid2`)
- Message structure: `messageId`, `senderId`, `text`, `timestamp`
- User discovery and friend list functionality
- Compatible with existing Android app chat structure

### Trip Planning System
- Interactive map of Armenia with regional boundaries
- Click-to-select cities from different marzes (provinces)
- Trip creation with title, description, and city list
- Trip status management (draft/finalized)
- AI-powered route optimization (placeholder implementation)
- Trip persistence in Firebase under user-specific paths

### Map Integration
- Leaflet.js map centered on Armenia
- Regional polygons for 11 Armenian marzes
- City markers with coordinates
- Interactive selection and route visualization
- Responsive design for mobile and desktop

## Data Flow

### User Authentication Flow
1. User registers with email, password, and username
2. Firebase creates authentication record
3. Username uniqueness checked in Firebase Realtime Database
4. User profile stored at `/users/{uid}`
5. Session established and user redirected to trips page

### Chat Flow
1. User selects target user from user list
2. Chat ID generated as `combinedUid1_uid2`
3. Messages stored at `/chats/{chatId}/messages/{messageId}`
4. Real-time listeners update chat interface
5. Message structure matches Android app requirements

### Trip Planning Flow
1. User accesses interactive Armenia map
2. Clicks on regions to reveal cities
3. Selects cities to add to trip itinerary
4. Fills trip details (name, description, status)
5. Trip saved to `/trips/{uid}/{tripId}`
6. Optional AI optimization reorders cities

## External Dependencies

### Firebase Services
- **Firebase Authentication**: User registration and login
- **Firebase Realtime Database**: Chat messages and trip data
- **Configuration**: Environment-based Firebase config

### UI Libraries
- **Radix UI**: Accessible component primitives
- **Shadcn/ui**: Pre-built component library
- **Leaflet**: Interactive mapping functionality
- **Font Awesome**: Icon library for UI elements

### Development Tools
- **Replit Integration**: Development environment support
- **ESBuild**: Production bundling
- **TypeScript**: Type safety and development experience

## Deployment Strategy

### Development Environment
- Vite development server with HMR
- Express backend with automatic restarts
- Firebase emulators for local testing
- Replit development banner integration

### Production Build
- Vite builds client to `dist/public`
- ESBuild bundles server to `dist/index.js`
- Static file serving through Express
- Environment variable configuration

### Database Strategy
- Drizzle migrations in `migrations/` directory
- PostgreSQL for relational data and sessions
- Firebase Realtime Database for real-time features
- Database URL configuration through environment variables

## Changelog
- July 04, 2025: Initial setup with Firebase chat and map-based trip planning
- July 04, 2025: MAJOR PIVOT - Removed map-based region selection, implemented core changes:
  * Replaced interactive map with simple city button selector and search
  * Added Friends system with username search, friend requests, accept/reject functionality
  * Added Group Chat system for creating chats with selected friends
  * Added Cars system with driver/passenger booking workflow
  * Updated navigation to include Friends, Cars pages
  * Maintained Firebase compatibility with Android app structure

## User Preferences

Preferred communication style: Simple, everyday language.