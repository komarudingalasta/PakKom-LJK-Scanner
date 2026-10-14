# PakKom LJK Scanner v1.8.6 — Precision Layout Fix

Perbaikan: label Puluhan/Satuan tidak bertumpuk dengan heading Tingkat Kelas/Nomor Absen pada A5 dan A4; kotak identitas dan posisi bubble tetap merujuk pada koordinat JSON yang sama dengan pembaca OMR. Penanda sudut digambar dari `registration_marks_mm` pada keempat sudut.

Validasi: `python tools/check_precision.py` mengecek seluruh 15 file template (12 kombinasi format + 3 alias), batas kertas, keempat penanda sudut, jarak antarbubble, dan posisi identitas. Pengujian ini bersifat geometris; belum membuktikan hasil cetak dan pemindaian fisik.

Build: unggah isi ZIP ke root repository GitHub yang sudah ada lalu jalankan GitHub Actions. Cetak PDF pada 100%/Actual Size, tanpa Fit to Page. Uji keempat marker dan hasil pemindaian pada kertas cetak sebelum pemakaian ujian.
