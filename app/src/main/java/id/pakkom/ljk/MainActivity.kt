package id.pakkom.ljk

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var paper: Spinner
    private lateinit var exam: EditText
    private lateinit var status: TextView
    private var pendingPaper = "A4"
    private var pendingExam = ""
    private val saveRequest = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 24.dp, 24.dp, 20.dp)
            setBackgroundColor(Color.rgb(247, 249, 250))
        }
        setContentView(ScrollView(this).apply { addView(root) })
        fun label(value: String, size: Float = 16f) = TextView(this).apply {
            text = value; textSize = size; setTextColor(Color.rgb(22, 73, 69))
            setPadding(0, 10.dp, 0, 8.dp); root.addView(this)
        }
        label("PakKom LJK Scanner", 25f)
        label("v1.1 - Generator LJK Universal Android", 14f)
        label("Ukuran kertas")
        paper = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("A4", "A5"))
            root.addView(this)
        }
        label("Kode ujian (catatan untuk guru)", 14f)
        exam = EditText(this).apply { setSingleLine(true); setText("MTK-9-001"); root.addView(this) }
        label("50 soal | A-E | Kode peserta 3 digit (000-999) | Paket A-D", 14f)
        label("LJK bersifat universal: siswa mengisi nama, kelas, kode peserta dan paket di kertas. Pastikan kode peserta unik dalam ujian.", 13f)
        root.addView(Button(this).apply {
            text = "BUAT DAN SIMPAN PDF LJK"
            setOnClickListener { createPdf() }
        })
        status = TextView(this).apply {
            text = "Cetak PDF pada skala 100% (Actual size)."
            textSize = 13f; root.addView(this)
        }
        label("Scanner OMR belum tersedia pada versi ini.", 13f)
    }

    private fun createPdf() {
        pendingPaper = paper.selectedItem.toString()
        pendingExam = exam.text.toString().trim()
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "PakKom LJK Universal $pendingPaper 50 Soal.pdf")
        }
        startActivityForResult(intent, saveRequest)
    }

    @Deprecated("Minimal project activity result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != saveRequest || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            val layout = assets.open("coordinates-$pendingPaper.json").bufferedReader().use { JSONObject(it.readText()) }
            contentResolver.openOutputStream(uri)?.use { LjkPdf(layout).write(it) }
                ?: error("Tidak dapat membuka file PDF")
            status.text = "PDF $pendingPaper berhasil disimpan. Cetak skala 100%. Kode ujian: $pendingExam"
            Toast.makeText(this, "PDF berhasil dibuat", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            status.text = "Gagal membuat PDF: ${e.message}"
        }
    }
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
