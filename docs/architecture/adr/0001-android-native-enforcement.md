# ADR 0001: Android Native Enforcement Strategy

## Context
Rakizz needs to actively prevent a student from accessing designated apps (e.g., TikTok, Instagram) during specific time windows, or when they exceed daily limits, unless they pass a study quiz. We must decide how to architect this enforcement on the Android platform.

## Options Considered

### Option 1: Mobile Device Management (MDM) / Device Owner
- **Description**: Profile installed via enterprise enrollment requiring a fresh device wipe to act as a Device Owner. Can rigorously blacklist arbitrary apps with no bypass.
- **Pros**: 100% impenetrable. Cannot be easily uninstalled by a tech-savvy student without factory wiping.
- **Cons**: Severe onboarding friction. Not suited for general B2C consumer app distributions on the Google Play Store outside of managed school networks.

### Option 2: `PACKAGE_USAGE_STATS` + Accessibility Service + Overlay Permission
- **Description**: Standard B2C parental control method. 
  - `PACKAGE_USAGE_STATS` (Usage Access) calculates daily metrics. 
  - An Accessibility Service (`AccessibilityService`) monitors `TYPE_WINDOW_STATE_CHANGED` events. 
  - If a banned `package_name` launches, the Rakizz app fires an Intent to launch a full-screen `System Alert Window` (Overlay) blocking the view and demanding they complete a study quiz.
- **Pros**: Fits standard consumer distribution. No factory device wipes required. Can launch rich dynamic UI.
- **Cons**: High likelihood of Android OS killing the background service (`Doze`). The student can bypass it by booting into Safe Mode and manually revoking the Accessibility or Usage Access permissions in settings.

## Decision
**We will proceed with Option 2 (`PACKAGE_USAGE_STATS` + Accessibility Service).** 
Option 1's MDM enterprise requirements alienate the K-12 mass-consumer audience and significantly delay the MVP go-to-market.

## Consequences & Constraints
1. **Bypass Reality**: We acknowledge and must document that a tech-savvy teenager can bypass Rakizz by circumventing permissions.
2. **Graceful Failures**: The Android client must be robust enough to notify the Web Dashboard backend if its `Accessibility Service` or `Usage Access` permissions are dropped, alerting the parent of the bypass immediately instead of silently failing.
3. **Play Store Review**: Google Play heavily scrutinizes Accessibility Services that do not aid disabled users. We must prepare a clear justification in the Play Console regarding the "Parental Control" exception policy.
