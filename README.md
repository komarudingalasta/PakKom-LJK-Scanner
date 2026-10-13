# PakKom LJK Scanner v1.5.4 — OMR Calibration Fix (uji coba)

Basis: source v1.5.3. **Tidak mengubah deteksi empat sudut, homografi, layout, atau tampilan kamera.**

## Perubahan
- Pisahkan fungsi `selectDigit()` dan `selectAnswer()`.
- Kembalikan aturan pemilihan kode peserta tepat seperti v1.5.2, agar pengetatan jawaban tidak lagi memengaruhi angka.
- Jawaban: skor di bawah 0,30 dianggap kosong (`-`), 0,30–0,40 dianggap tidak pasti (`?`), di atas 0,40 harus lolos uji jarak skor dengan pilihan lain.
- Diagnostik skor jawaban tidak pasti tetap ditampilkan.

**Penting:** batas jawaban masih hipotesis, bukan hasil kalibrasi statistik. Tidak dijamin memperbaiki nomor 4 dan 6; dapat membuat arsiran tipis menjadi `?` atau `-`. Periksa ulang semua jawaban yang diarsir. Jangan gunakan untuk penilaian resmi sampai lolos pengujian pada foto cetak nyata.

## Uji regresi wajib
1. Scan lembar yang sama dengan v1.5.2 dan v1.5.3. Pastikan kode peserta kembali terbaca.
2. Pastikan nomor 4 dan 6 kosong (`-`), bukan `A` atau `?`.
3. Pastikan nomor 1, 3, 5, 11, 12, 14, 15 tetap sesuai arsiran.
4. Uji beberapa foto terang, berbayang, dan sedikit miring.
5. Jika masih gagal, gunakan diagnostik skor dan foto asli untuk kalibrasi berbasis data, bukan terus menaikkan threshold.

Unggah isi ZIP ke akar repository GitHub dan build dengan GitHub Actions.
