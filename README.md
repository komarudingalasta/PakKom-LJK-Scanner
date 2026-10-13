# PakKom LJK Scanner v1.8.2 — Identitas Universal

## Perubahan
- PDF LJK minimalis dengan identitas kelas 01–12, rombel A–L, dan nomor absen 01–36.
- Lima kelompok arsiran: puluhan tingkat (0–1), satuan tingkat (0–9), rombel (A–L), puluhan absen (0–3), satuan absen (0–9).
- Pembacaan identitas oleh OMR dari koordinat `identity_bubbles` yang sama dengan generator PDF. Hasil misalnya `09F-18`.
- Spasi antar kelompok 5 soal sedikit lebih lebar, sesuai koordinat OMR yang dihasilkan.
- Kode format berubah menjadi `-ID2` untuk mencegah tertukar dengan PDF versi lama.

## Penting
Semua format 12 kombinasi memerlukan pengujian cetak 100% dan scan fisik sebelum digunakan untuk penilaian resmi. Template lama (kode peserta 3 digit) tidak cocok dengan pembaca identitas baru. Source belum berhasil diuji build Android di lingkungan ini.
