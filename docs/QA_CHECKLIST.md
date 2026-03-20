# QA Checklist — CaloriesTracker

## Camera Permission — Denial Flow

- Launch the app for the first time on a fresh install.
- Navigate to Camera AI screen.
- When the system permission dialog appears, tap **Deny**.
- Expected: app shows a rationale message explaining why the permission is needed; a "Grant Permission" button is visible.
- Tap "Grant Permission" — system dialog appears again.
- Tap **Deny** again.
- Expected: app shows a permanent-denial message with a button that opens app Settings (ACTION_APPLICATION_DETAILS_SETTINGS).
- In Settings, grant the permission manually, return to the app.
- Expected: camera preview loads correctly.

## Camera Permission — Permanent Denial Flow

- Grant camera permission once, then revoke it manually in Settings.
- Return to the app and navigate to Camera AI screen.
- Expected: app detects permanent denial on first attempt (no dialog shown by system), displays Settings-redirect message immediately.

## Barcode — NOT_FOUND Flow

- Navigate to Camera AI screen.
- Scan a valid barcode or trigger barcode scan with a barcode that returns no result from OpenFoodFacts / USDA.
- Expected: UI shows a "Product not found" message (not a crash, not a blank screen).
- Dismiss / retry button is visible.

## AI — UNRECOGNIZED Flow

- Navigate to Camera AI screen.
- Point camera at a non-food object or an image that the AI model cannot identify as food.
- Expected: UI shows an "Unrecognized food" message.
- User can dismiss and retry without restarting the flow.

## Offline Diary Browsing

- Disable network (airplane mode).
- Open the app — if not signed in, sign in first while online, then go offline.
- Navigate to the Diary screen.
- Expected: locally cached diary entries load from Room without error.
- Navigate between dates using the date selector.
- Expected: previously viewed dates show cached data; dates with no cached data show empty state, not an error.
- Re-enable network.
- Expected: sync resumes without requiring app restart.

## Language Switch — EN to ES

- Open Settings screen.
- Change language to **Spanish (ES)**.
- Expected: all visible strings switch to Spanish immediately or after app restart (depending on implementation).
- Verify Diary screen labels, Search screen labels, and Settings screen labels are all in Spanish.
- Switch back to **English (EN)**.
- Expected: strings revert to English.

## Theme Switch — Light to Dark

- Open Settings screen.
- Switch to **Dark** theme.
- Expected: entire app switches to dark color scheme without recomposition artifacts.
- Navigate to Diary, Search, Camera AI, Analytics screens and verify no contrast issues.
- Switch to **Light** theme.
- Expected: entire app switches back to light color scheme.

## Sign-Out Cancels WorkManager Sync

- Sign in with a valid account.
- Verify a sync WorkManager job is enqueued (can be confirmed via Android Studio App Inspection → Background Task Inspector).
- Tap Sign Out.
- Expected: all pending / enqueued sync WorkManager tasks for the user are cancelled.
- Confirm in Background Task Inspector that no sync jobs remain in ENQUEUED or RUNNING state.
- Sign in again.
- Expected: sync job is re-enqueued after sign-in.
