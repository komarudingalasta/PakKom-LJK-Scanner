# PakKom LJK Scanner v1.5.1 — Lighting & Shadow Correction

Android Kotlin source for GitHub Actions. Built on v1.5; registration mark detection and perspective validation are unchanged.

## Changes
- Bubble interior sampling uses 48% of nominal radius, excluding the printed outline.
- Local paper brightness is estimated from the 70th percentile of a surrounding annulus, reducing sensitivity to uneven illumination and stray ink.
- Local relative contrast is used instead of a fixed absolute grayscale threshold.
- Existing ambiguous/empty answer logic, camera, gallery, layouts and PDF generation are preserved.

## Important
This is an experimental OMR improvement, **not yet validated on real printed sheets or built on a device**. Compare against v1.5 with the same sheet, including code 213 and intentionally blank answers. If accuracy worsens, revert to v1.5. Avoid harsh shadows and glare; no algorithm can recover details hidden by severe darkness.

## Install
Extract the ZIP into the root of the existing GitHub repository, commit, run the existing GitHub Actions APK workflow, and test the resulting APK.
