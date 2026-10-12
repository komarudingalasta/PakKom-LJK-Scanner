# PakKom LJK Scanner v1.3.1 — OMR Fix

Hotfix untuk hasil scan v1.3 yang sering membaca bulatan hitam sebagai kosong dan kode peserta sebagai ???. Tidak ada database, analisis, atau penyimpanan.

## Perubahan
- Skor bulatan berdasarkan proporsi piksel gelap pada **bagian tengah** (mengabaikan tepi bulatan tercetak) serta kontras terhadap area luar.
- Pencarian kecil ±2 piksel mengurangi dampak pergeseran registrasi.
- Pemilihan jawaban berdasarkan skor terbaik dan perbedaan terhadap skor kedua; jawaban ambigu diberi tanda `?`, bukan ditebak.
- Lingkaran overlay hijau = pilihan terbaca; biru = lainnya.
- Kamera eksternal/galeri tetap mengikuti v1.3.

## Batasan penting
Ini adalah perbaikan algoritma, **belum terbukti akurat pada foto asli**. Deteksi penanda dan koordinat masih mengikuti v1.3. Uji dengan PDF asli yang dicetak 100% (bukan screenshot/hasil crop) dan arsiran hitam yang merata. Jika salah, kirim **foto asli** dan overlay untuk diagnosis kalibrasi; jangan gunakan hasil untuk nilai resmi.

## Upload GitHub
Ekstrak ZIP dan upload semua **isi folder** ke root repository, termasuk `.github`, commit satu kali. GitHub Actions > Build Android APK > Artifacts.
