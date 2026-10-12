package id.pakkom.ljk

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var personal: RadioButton
    private lateinit var name: EditText
    private lateinit var nis: EditText
    private lateinit var classroom: EditText
    private lateinit var exam: EditText
    private lateinit var pack: Spinner
    private lateinit var status: TextView
    private var pending: LjkPdf.Student? = null
    private var pendingPersonal = false
    private val saveRequest = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22.dp, 24.dp, 22.dp, 20.dp)
            setBackgroundColor(Color.rgb(247,249,250))
        }
        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
        fun heading(t: String, size: Float = 18f) = TextView(this).apply {
            text=t; textSize=size; setTextColor(Color.rgb(21,72,68)); setPadding(0,8.dp,0,10.dp)
            root.addView(this)
        }
        fun field(label: String, default: String = ""): EditText {
            heading(label, 13f)
            return EditText(this).apply { setSingleLine(true); setText(default); textSize=16f; root.addView(this) }
        }
        heading("PakKom LJK Scanner", 25f)
        heading("v1.0 · Generator PDF LJK Android", 14f)
        val radio = RadioGroup(this).apply { orientation=RadioGroup.HORIZONTAL }
        personal = RadioButton(this).apply { text="Personal"; isChecked=true }
        val general = RadioButton(this).apply { text="Umum" }
        radio.addView(personal); radio.addView(general); root.addView(radio)
        name=field("Nama siswa", "Siswa Contoh")
        nis=field("NIS (8 digit untuk LJK Umum)", "20260001")
        classroom=field("Kelas", "9A")
        exam=field("Kode ujian (tanpa simbol |)", "MTK-9-001")
        heading("Paket soal",13f)
        pack=Spinner(this)
        pack.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item, listOf("A","B","C","D"))
        root.addView(pack)
        fun refresh() { val enabled=personal.isChecked; name.isEnabled=enabled; nis.isEnabled=enabled; classroom.isEnabled=enabled }
        radio.setOnCheckedChangeListener { _,_->refresh() }; refresh()
        val button=Button(this).apply { text="BUAT & SIMPAN PDF A4"; setOnClickListener { createPdf() } }
        root.addView(button)
        status=TextView(this).apply { text="PDF disimpan ke folder pilihan Anda. Cetak pada skala 100%."; textSize=13f }
        root.addView(status)
        heading("Tahap berikutnya: Live Scanner OMR (belum tersedia)", 13f)
    }
    private fun createPdf() {
        val isPersonal=personal.isChecked
        val student=LjkPdf.Student(name.text.toString().trim(),nis.text.toString().trim(),classroom.text.toString().trim(),exam.text.toString().trim(),pack.selectedItem.toString())
        if (student.exam.isEmpty() || student.exam.contains('|') || (isPersonal && (student.name.isEmpty() || student.nis.isEmpty() || student.classroom.isEmpty() || student.nis.contains('|')))) {
            Toast.makeText(this,"Lengkapi identitas dan kode ujian yang valid",Toast.LENGTH_LONG).show();return
        }
        pending=student;pendingPersonal=isPersonal
        val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type="application/pdf"
            putExtra(Intent.EXTRA_TITLE, "PakKom LJK ${if(isPersonal) "Personal ${student.nis}" else "Umum"}.pdf")
        }
        startActivityForResult(intent,saveRequest)
    }
    @Deprecated("Uses Activity result for compatibility with the minimal project")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode!=saveRequest || resultCode!=RESULT_OK) return
        val uri=data?.data ?: return
        val student=pending ?: return
        try {
            val layout=assets.open("omr_coordinates_v1.json").bufferedReader().use { JSONObject(it.readText()) }
            contentResolver.openOutputStream(uri)?.use { LjkPdf(layout).write(it,pendingPersonal,student) }
                ?: error("Tidak dapat membuka file tujuan")
            status.text="PDF berhasil disimpan. Cetak ukuran A4 dengan skala 100%."
            Toast.makeText(this,"PDF berhasil dibuat",Toast.LENGTH_LONG).show()
        } catch(e:Exception) {
            status.text="Gagal membuat PDF: ${e.message}"
            Toast.makeText(this,"Gagal: ${e.message}",Toast.LENGTH_LONG).show()
        }
    }
    private val Int.dp: Int get()=(this*resources.displayMetrics.density).toInt()
}
