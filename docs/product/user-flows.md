# Core User Flows

## 1. Parent Setup Flow
1. **Registration**: Parent registers an account on the Rakizz Web Admin UI.
2. **Student Linking**: Parent creates a sub-profile or linking code for the student.
3. **App Setup**: Student installs the Android app, authenticates using the linking code/credentials.
4. **Permissions**: Student grants `PACKAGE_USAGE_STATS` (Usage Access) and overlaid draw permissions to the Rakizz app.
5. **Policy Definition**: Parent logs into the Web Dashboard, selects the Student, and registers an app-blocking rule (e.g., time-window rule or quiz-based unlock rule).
6. **Sync**: The Android app polls or receives a push notification, caching the new policy locally.

## 2. The Quiz-Based Temporary Unlock Flow
1. **Trigger**: A student is blocked from opening a distracting app (e.g., Instagram).
2. **Intervention Screen**: An overlay appears stating "App Blocked. Take a Quiz to unlock for 30 minutes?".
3. **Quiz Generation**: Student selects "Yes". The backend's AI engine dynamically generates a 5-question quiz from their recently uploaded "Biology Chapter 3" material.
4. **Assessment**: Student completes the quiz in the app.
5. **Reward**: Upon scoring > 80%, the backend issues a "Temporary Policy Exception" token to the device.
6. **Execution**: The Android app locally logs the exception and allows the distracting app to be launched for 30 minutes. 

## 3. Assignment Tracking Flow
1. **Creation**: Student or Parent inputs a new assignment ("Read Chapter 4") with a deadline.
2. **Sync**: Backend persists the assignment, making it visible on the student's Android dashboard under "To-Do".
3. **Completion**: Student marks the assignment as completed once finished.
4. **Validation**: Parent views completed assignments on the Next.js reporting dashboard.

## 4. Usage Data Sync Flow
1. **Collection**: Rakizz Android App periodically wakes up and retrieves app usage totals from the OS (`UsageStatsManager`).
2. **Aggregation**: The app aggregates these statistics every 15 minutes locally.
3. **Transmission**: The app securely POSTs the aggregated payload to the FastAPI `/usage/sync` endpoint.
4. **Visualization**: Parent accesses `Web Admin` to observe which apps were utilized and for how long.
