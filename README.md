# PakKom LJK Scanner v1.2 — Prototype OMR Android

Source Android GitHub-ready. Camera capture (full resolution via FileProvider), gallery import, detect four black registration marks, rectify perspective using Android Matrix, sample 3-digit participant code and answer bubbles, show blanks and ambiguous marks. Processing is offline on-device.

## Supported scanner formats
- A5 20 questions A–D (custom approved preview)
- A5 30 questions A–E
- A4 50 questions A–E

**Important:** This is an experimental scanner, not production-ready. Marker detection and fill thresholds have NOT been calibrated against real printed photographs. Perspective normalization and orientation can fail; manually verify every result. It does not score or persist scans. The older v1.1 PDF generator has not been modified or connected to these three new scan layouts; the existing source was preserved separately as `LjkPdf.kt` but the UI focuses on scanning. Dynamic PDF generator remains future work.

## Upload to GitHub
Extract ZIP and upload all contents to the repository root, including hidden `.github`. Replace older files; remove obsolete workflows to avoid Pages builds. Go to Actions > Build Android APK, download debug artifact. Build is NOT verified here: no local Android SDK/Gradle environment. Do not rely on this APK for official grading until camera/photo tests pass.

## Usage
Choose correct format (A5 20 A-D etc), tap Photo LJK or Select Photo, inspect participant code and responses, review warnings. Use full-page photo with all four corner marks visible and minimal shadows. Printed page must be at actual size 100%.

## Technical
Kotlin, Android SDK 35, min 28, AndroidX Core FileProvider. No network, Firebase, OpenCV, or paid services. OMR thresholds in `OmrReader.kt` are preliminary and must be calibrated.
