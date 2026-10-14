# PakKom LJK Scanner v1.8.5 — Proportional Layout

- All six paper/count combinations (A5 10/20/30; A4 30/40/50) support A–D and A–E.
- Recomputed question bubble positions for all 12 combinations, with two balanced columns and an extra 2.8 mm (A5) / 3.5 mm (A4) gap after each five rows.
- A4 50: smaller 2.65 mm radius and ~6.04 mm row pitch to avoid touching circles.
- A5 30: 2.35 mm radius and ~5.49 mm row pitch to avoid touching circles.
- Shifted identity bubbles and panel down to avoid collisions with name/class labels.
- Scanner and PDF generator read the same scan JSON layouts; OMR reading algorithm itself is unchanged.
- `python tools/check_proportions.py` checks all scan layouts for answer bubble overlap and paper bounds.

**Not yet validated:** Android APK build, 100%-scale paper printing, real-camera OMR scan. Test before operational school use.
