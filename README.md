# PakKom LJK Scanner v1.6.1 — Build Hotfix

Perbaikan tiga error Kotlin pada LiveScannerActivity.kt yang terlihat di GitHub Actions:

- Baris 52: `layout` di dalam `TextView.apply` mengacu pada properti view (LayoutParams), bukan `JSONObject` Activity. Diganti menjadi `this@LiveScannerActivity.layout.optString("format_id")`.
- Baris 72–73: signature override `onRequestPermissionsResult` diubah dari `Array<out String>` menjadi `Array<String>` agar cocok dengan superclass AndroidX ComponentActivity.
- Version code/name menjadi 161 / 1.6.1.

Tidak ada perubahan algoritma OMR.

**Validasi:** source diperiksa terhadap tiga error yang diberikan. Build Android Gradle belum dijalankan di lingkungan ini; lakukan build GitHub Actions dan kirim error pertama jika masih gagal.

Cara pemasangan: ekstrak ZIP, unggah semua isi ke root repository (bukan folder pembungkus), commit, lalu cek Actions.
