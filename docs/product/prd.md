# Product Requirements Document (PRD)

## 1. Objective
Rakizz is an Android-first study-focus ecosystem. Its primary goal is to help students build focus and discipline through parent-configurable app-blocking policies. Uniquely, students can earn back unrestricted device time by passing AI-generated quizzes formulated directly from their uploaded study materials.

## 2. Target Audience
This platform targets K-12 and early university students whose parents or guardians wish to enforce better digital habits while tying app-usage rewards directly to educational outcomes.

## 3. User Roles
- **Student**: Operates exclusively on the Android application. Subject to enforcement policies. Tasks include uploading study materials, managing assignments, and taking AI-generated quizzes to unlock access to non-educational apps.
- **Parent**: Operates primarily via the Next.js Web Admin dashboard (and optionally the parent portal of the Android app). Configures policies, monitors student usage logs, and manages assignments/materials.
- **Admin**: System administrators maintaining platform health and observing aggregated usage trends.

## 4. Key Features
1. **Authentication & Roles**: Secure login ensuring Students cannot access Parent-level rule configurations.
2. **Materials Repository**: A centralized hub for uploading and organizing study documents/links.
3. **Assignments Tracker**: Task tracking with deadline management for students.
4. **AI Assessment Engine**: Dynamic generation of quizzes from uploaded materials. Passing these quizzes serves as the primary mechanism for the "Quiz-Based Temporary Unlock" policy.
5. **App Enforcement Engine**: Android-native service running in the background to enforce time limits, schedules, and active blocks on distracting apps.
6. **Reporting Dashboard**: A centralized view for parents detailing app usage, quiz scores, and study material engagement.

## 5. Non-Functional Requirements
- **Security**: Strict role-based access control; Android client cannot be fully trusted; encrypted traffic.
- **Performance**: Android app enforcement must have a minimal battery and memory overhead to prevent OS termination.
- **Reliability**: Graceful failing when offline or when permissions are missing.
