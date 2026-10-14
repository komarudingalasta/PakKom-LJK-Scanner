# PakKom LJK Scanner v1.8.7 — Settings Icon Fix

- Ikon Pengaturan pada header menggunakan Android Vector Drawable (`res/drawable/ic_settings_gear.xml`), bukan Unicode atau Canvas icon khusus.
- Warna hijau tua #075E59; ukuran ikon 24dp, area sentuh 44dp (sebelumnya).
- Klik ikon tetap memanggil `showOptions()`.
- Generator PDF, template OMR, pembacaan kamera, dan fitur lain tidak diubah.
- Belum diuji build APK atau di perangkat fisik.
