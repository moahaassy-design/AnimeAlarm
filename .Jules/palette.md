## 2024-05-21 - Confirmation for Destructive Actions

**Learning:** Adding a confirmation dialog before a destructive action (like deleting an alarm) is a crucial UX pattern to prevent accidental data loss. This is especially important in a mobile app where accidental taps are common.

**Action:** I will apply this pattern to any future destructive actions I implement in this application. This will involve hoisting state for the dialog, displaying an `AlertDialog`, and externalizing strings for the dialog's content.
