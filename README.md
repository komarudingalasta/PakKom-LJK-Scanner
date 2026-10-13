# PakKom LJK Scanner v1.6.8 — Smart Scan Experience

Based on v1.6.7, retaining its home UI and OMR answer-decoding rules.

## Changes
- Live Camera AUTO checks four real fiducial marker positions in consecutive preview frames.
- Captures only after three successive frames with marker movement below 1.2% of image diagonal.
- Simple image-contrast/blur warning; AUTO remains OFF by default.
- Full-resolution still photo is processed by the existing OMR engine, which validates perspective.
- Manual FOTO remains available regardless of AUTO validation.
- Auto capture disabled while photo is being saved; original CameraX pipeline retained.

## Limitations
- Preview image sharpness threshold is heuristic and may require device-specific tuning.
- Registration-marker detection in preview is CPU-intensive; frames are sampled every 1.7 seconds.
- This version does not add batch storage or change answer recognition.
- Test on real printed A5 and A4 forms before relying on automatic capture.
