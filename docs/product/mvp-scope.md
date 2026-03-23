# MVP Scope vs. Deferred Scope

## 1. MVP Scope (In)

### Core User Capabilities
- **Authentication**: JWT-based secure authentication supporting Student, Parent, and Admin roles.
- **Study Materials Repository**: Ability to upload text, links, or documents that students must study.
- **Assignments Tracker**: Students can log tasks with deadlines. Parents can view and track completion.
- **AI Quiz Generation**: The platform generates automated assessments against particular materials to test student knowledge.

### App Blocking & Enforcement (Android Only)
- **Time-Window Rules**: Blocks explicitly configured intervals (e.g., "Monday, 8 AM to 3 PM").
- **Daily-Usage-Limit Rules**: Cumulative application caps (e.g., "Max 60 mins of Social Media daily").
- **Quiz-Based Temporary Unlock**: The ability for a student to temporarily unblock an app or device by passing an AI quiz generated from their study materials.
- **Local Policy Engine**: Native background Android service checking usage statistics, cross-referencing with active parent policies to block targeted apps via overlays or accessibility redirection.

### Reporting & Dashboards
- **Parent Web Dashboard**: A Next.js-based interface for managing usage limits, overseeing quiz results, and uploading materials.
- **Usage Metrics Sync**: Android usage limits regularly synced back to the backend for parent analysis.

## 2. Deferred Scope (Strictly Excluded)

- **iOS Application**: Apple's sandbox restricts overlay-based blocking or raw usage-stat access at the consumer level, making the MVP core value prop impossible without strict MDM configuration. Out of scope entirely.
- **Payments / Monetization**: Subscriptions, paywalls, and billing infrastructure are excluded from MVP to focus on product validation.
- **School SIS / LMS Integrations**: Integrations with Canvas, Blackboard, Google Classroom, and others exist as a long-term goal but are excluded from MVP.
- **In-App Chat / Messaging**: Communication layers between parents and students will remain outside the app.
- **Browser Extensions**: Web blocking tracking via browser extensions is deferred; enforcement focuses solely on the Android ecosystem in MVP.
- **Extravagant Gamification**: Leaderboards, avatar shops, and social loops distract from the core habit-building and are excluded.
