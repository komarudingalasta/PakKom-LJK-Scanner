package id.pakkom.ljk

import androidx.activity.ComponentActivity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.FileProvider
import org.json.JSONObject
import org.json.JSONArray
import java.io.File

/** v1.6.9: balanced homepage; OMR algorithm and JSON assets unchanged. */
class MainActivity : ComponentActivity() {
    private lateinit var mode: Spinner
    private lateinit var root: LinearLayout
    private var photoUri: Uri? = null
    private val cameraRequest = 101
    private val liveRequest = 103
    private val galleryRequest = 102
    private val pdfRequest = 104
    private var pdfTitle = ""
    private var pdfSchool = ""
    private var lastImage: Bitmap? = null
    private var lastReport: String? = null
    private var lastCode: String = ""
    private var lastFormat: String = ""
    private var lastAnswers: List<String> = emptyList()
    private var lastWarnings: List<String> = emptyList()
    private var isReading = false
    private var scanningKey = false
    private data class FormatSpec(val paper:String,val count:Int,val options:String) {
        val asset get() = "scan-$paper-$count-$options.json"
        val label get() = "$paper · $count Soal · ${if(options=="ABCD") "A–D" else "A–E"}"
    }
    private val specs = listOf(
        FormatSpec("A5",10,"ABCD"), FormatSpec("A5",10,"ABCDE"),
        FormatSpec("A5",20,"ABCD"), FormatSpec("A5",20,"ABCDE"),
        FormatSpec("A5",30,"ABCD"), FormatSpec("A5",30,"ABCDE"),
        FormatSpec("A4",30,"ABCD"), FormatSpec("A4",30,"ABCDE"),
        FormatSpec("A4",40,"ABCD"), FormatSpec("A4",40,"ABCDE"),
        FormatSpec("A4",50,"ABCD"), FormatSpec("A4",50,"ABCDE")
    )
    private fun formatSize() = specs[selectedFormatIndex].count
    private fun formatChoices() = specs[selectedFormatIndex].options.map { it.toString() }
    private fun keyStorageName(): String = when(selectedFormatIndex) {
        2 -> "key_0"  // Legacy A5 20 A-D
        5 -> "key_1"  // Legacy A5 30 A-E
        11 -> "key_2" // Legacy A4 50 A-E
        else -> "key_v181_${selectedFormatIndex}"
    }
    private fun loadKey(): MutableList<String> {
        val size = formatSize()
        val saved = getSharedPreferences("pakkom_keys", MODE_PRIVATE).getString(keyStorageName(), null)
        if(saved == null) return MutableList(size) { "" }
        return try {
            val a = JSONArray(saved)
            MutableList(size) { i -> if(i < a.length()) a.optString(i).takeIf { it in formatChoices() } ?: "" else "" }
        } catch (_: Exception) { MutableList(size) { "" } }
    }
    private fun saveKey(key: List<String>) {
        getSharedPreferences("pakkom_keys", MODE_PRIVATE).edit().putString(keyStorageName(), JSONArray(key).toString()).apply()
    }
    private fun showKeyEditor() {
        val page = newPage()
        RetroUi.add(page, RetroUi.action(this,"‹  Kembali",RetroUi.cream,RetroUi.green){showHome()},0,12)
        RetroUi.add(page,RetroUi.text(this,"Kunci Jawaban",23f,true,RetroUi.green),0,5)
        val formats = specs.map { it.label }
        RetroUi.add(page,RetroUi.text(this,formats[selectedFormatIndex],12f,false,RetroUi.brown),0,12)
        val key = loadKey()
        val status=RetroUi.text(this,"Terisi ${key.count { it.isNotEmpty() }} dari ${key.size} soal",13f,true,RetroUi.green)
        RetroUi.add(page,status,0,12)
        val grid = RetroUi.panel(this)
        val choices = listOf("—") + formatChoices()
        for(i in key.indices) {
            val line=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL }
            line.addView(RetroUi.text(this,"%02d".format(i+1),14f,true,RetroUi.green),LinearLayout.LayoutParams(50.dp,-2))
            val spinner=Spinner(this)
            spinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,choices)
            spinner.setSelection(if(key[i].isEmpty()) 0 else choices.indexOf(key[i]).coerceAtLeast(0))
            spinner.onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent:android.widget.AdapterView<*>?) {}
                override fun onItemSelected(parent:android.widget.AdapterView<*>?,view:View?,position:Int,id:Long) {
                    key[i]=if(position==0) "" else choices[position]
                    status.text="Terisi ${key.count { it.isNotEmpty() }} dari ${key.size} soal"
                }
            }
            line.addView(spinner,LinearLayout.LayoutParams(0,48.dp,1f))
            RetroUi.add(grid,line)
        }
        RetroUi.add(page,grid,0,12)
        RetroUi.add(page,RetroUi.action(this,"SIMPAN KUNCI",RetroUi.orange){
            saveKey(key);toast("Kunci tersimpan di perangkat");showHome()
        },0,9)
        RetroUi.add(page,RetroUi.action(this,"SCAN LEMBAR KUNCI",RetroUi.green){
            saveKey(key);scanningKey=true;openLive()
        },0,9)
        RetroUi.add(page,RetroUi.text(this,"Kunci disimpan offline berdasarkan format. Jawaban kosong tidak dihitung benar. Hasil scan kunci harus diperiksa sebelum disimpan.",12f,false,RetroUi.brown))
    }
    private fun showScannedKey(answers:List<String>) {
        val options=formatChoices()
        val proposed=answers.map { if(it in options) it else "" }.toMutableList()
        val unclear=answers.count { it !in options }
        android.app.AlertDialog.Builder(this).setTitle("Konfirmasi kunci hasil scan")
            .setMessage("${proposed.count { it.isNotEmpty() }} jawaban terbaca; $unclear kosong/tidak jelas. Periksa semua nomor pada editor sebelum digunakan.")
            .setPositiveButton("Buka editor") { _,_ -> saveKey(proposed);showKeyEditor() }
            .setNegativeButton("Batal") { _,_ -> showHome() }.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun newPage(): LinearLayout {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(RetroUi.paper)
            setPadding(20.dp, 18.dp, 20.dp, 24.dp)
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; addView(page) }
        setContentView(scroll)
        root = page
        return page
    }

    /** v1.6.9: balanced, responsive homepage with consistent native vector icons. */
    private fun showHome() {
        val page=newPage()
        page.setPadding(20.dp,20.dp,20.dp,20.dp)
        val header=LinearLayout(this).apply {orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        val brandBox=LinearLayout(this).apply {
            gravity=Gravity.CENTER
            background=RetroUi.background(RetroUi.green,12.dp.toFloat(),RetroUi.green,0)
            addView(ScannerIconView(this@MainActivity,ScannerIconView.Kind.SCAN,RetroUi.cream),LinearLayout.LayoutParams(27.dp,27.dp))
        }
        header.addView(brandBox,LinearLayout.LayoutParams(44.dp,44.dp).apply {rightMargin=10.dp})
        val identity=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL}
        RetroUi.add(identity,RetroUi.text(this,"PakKom LJK",21f,true,RetroUi.green))
        RetroUi.add(identity,RetroUi.text(this,"SMART SCANNER  ·  v1.8.0",10f,true,RetroUi.brown),2)
        header.addView(identity,LinearLayout.LayoutParams(0,-2,1f))
        val settings=LinearLayout(this).apply {
            gravity=Gravity.CENTER;isClickable=true;isFocusable=true
            contentDescription="Pengaturan dan bantuan"
            addView(ScannerIconView(this@MainActivity,ScannerIconView.Kind.SETTINGS,RetroUi.green),LinearLayout.LayoutParams(25.dp,25.dp))
            setOnClickListener {showOptions()}
        }
        header.addView(settings,LinearLayout.LayoutParams(44.dp,44.dp))
        RetroUi.add(page,header,0,19)

        val formatRow=RetroUi.panel(this,RetroUi.cream)
        RetroUi.add(formatRow,RetroUi.text(this,"FORMAT AKTIF",10f,true,RetroUi.brown),0,8)
        val formatCols=LinearLayout(this).apply {orientation=LinearLayout.HORIZONTAL}
        val active=specs[selectedFormatIndex]
        val values=listOf(active.paper,active.count.toString(),if(active.options=="ABCD") "A–D" else "A–E")
        listOf("Kertas","Soal","Pilihan").forEachIndexed {i,label ->
            val column=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL}
            RetroUi.add(column,RetroUi.text(this,label,10f,false,RetroUi.brown))
            RetroUi.add(column,RetroUi.text(this,values[i],16f,true,RetroUi.green),2)
            formatCols.addView(column,LinearLayout.LayoutParams(0,-2,1f))
        }
        RetroUi.add(formatRow,formatCols)
        mode=Spinner(this).apply {
            adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,
                specs.map { it.label })
            setSelection(selectedFormatIndex)
            visibility=View.GONE
        }
        RetroUi.add(formatRow,mode)
        RetroUi.add(formatRow,RetroUi.action(this,"Atur format & buat PDF",RetroUi.greenLight,RetroUi.green){showGenerator()},10)
        RetroUi.add(page,formatRow,0,14)

        val hero=RetroUi.panel(this,RetroUi.greenLight).apply {setPadding(15.dp,14.dp,15.dp,14.dp)}
        val heading=LinearLayout(this).apply {orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        val scanBox=LinearLayout(this).apply {
            gravity=Gravity.CENTER;background=RetroUi.background(RetroUi.cream,12.dp.toFloat(),RetroUi.cream,0)
            addView(ScannerIconView(this@MainActivity,ScannerIconView.Kind.SCAN,RetroUi.green),LinearLayout.LayoutParams(31.dp,31.dp))
        }
        heading.addView(scanBox,LinearLayout.LayoutParams(53.dp,53.dp).apply {rightMargin=12.dp})
        val desc=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL}
        RetroUi.add(desc,RetroUi.text(this,"Siap memindai",19f,true,RetroUi.green))
        RetroUi.add(desc,RetroUi.text(this,"Arahkan kamera ke seluruh lembar jawaban.",12f,false,RetroUi.brown),3)
        heading.addView(desc,LinearLayout.LayoutParams(0,-2,1f))
        RetroUi.add(hero,heading,0,12)
        val scanButton=LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER
            background=RetroUi.background(RetroUi.orange,12.dp.toFloat(),RetroUi.orange,0)
            minimumHeight=48.dp;isClickable=true;isFocusable=true
            contentDescription="Mulai Scan";setOnClickListener {openLive()}
        }
        scanButton.addView(ScannerIconView(this,ScannerIconView.Kind.CAMERA,RetroUi.cream),LinearLayout.LayoutParams(22.dp,22.dp).apply {rightMargin=9.dp})
        scanButton.addView(RetroUi.text(this,"Mulai Scan",16f,true,RetroUi.cream))
        RetroUi.add(hero,scanButton)
        val gallery=LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;minimumHeight=43.dp
            isClickable=true;isFocusable=true;contentDescription="Pilih dari Galeri";setOnClickListener {openGallery()}
        }
        gallery.addView(ScannerIconView(this,ScannerIconView.Kind.GALLERY,RetroUi.green),LinearLayout.LayoutParams(19.dp,19.dp).apply {rightMargin=8.dp})
        gallery.addView(RetroUi.text(this,"Pilih dari Galeri",14f,true,RetroUi.green))
        RetroUi.add(hero,gallery,6)
        RetroUi.add(page,hero,0,14)
        val keyCard=RetroUi.panel(this)
        val currentKey=loadKey()
        RetroUi.add(keyCard,RetroUi.text(this,"Kunci Jawaban",16f,true,RetroUi.green),0,5)
        RetroUi.add(keyCard,RetroUi.text(this,"${currentKey.count { it.isNotEmpty() }}/${currentKey.size} soal terisi · Tersimpan offline",12f,false,RetroUi.brown),0,10)
        RetroUi.add(keyCard,RetroUi.action(this,"Kelola Kunci Jawaban",RetroUi.greenLight,RetroUi.green){selectedLayoutSafely();showKeyEditor()})
        RetroUi.add(page,keyCard,0,14)

        val pdfCard=RetroUi.panel(this)
        RetroUi.add(pdfCard,RetroUi.text(this,"Buat & Unduh LJK",16f,true,RetroUi.green),0,5)
        RetroUi.add(pdfCard,RetroUi.text(this,"Judul ujian bebas · PDF minimalis · Cetak ukuran asli",12f,false,RetroUi.brown),0,10)
        RetroUi.add(pdfCard,RetroUi.action(this,"Buat LJK PDF",RetroUi.greenLight,RetroUi.green){ selectedLayoutSafely();showGenerator() })
        RetroUi.add(page,pdfCard,0,14)

        val guide=RetroUi.panel(this,RetroUi.cream).apply {setPadding(14.dp,13.dp,14.dp,13.dp)}
        val guideHeader=LinearLayout(this).apply {orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        guideHeader.addView(RetroUi.text(this,"Panduan posisi LJK",14f,true,RetroUi.green),LinearLayout.LayoutParams(0,-2,1f))
        guideHeader.addView(RetroUi.text(this,"4 penanda sudut",10f,false,RetroUi.brown))
        RetroUi.add(guide,guideHeader,0,9)
        RetroUi.add(guide,LjkGuideView(this))
        RetroUi.add(guide,RetroUi.text(this,"Pastikan keempat kotak hitam terlihat dan kertas tidak terpotong.",11f,false,RetroUi.brown).apply {gravity=Gravity.CENTER},9)
        RetroUi.add(page,guide)

        // Push footer toward the bottom on tall screens, but let ScrollView work on short screens.
        val spacer=View(this)
        page.addView(spacer,LinearLayout.LayoutParams(-1,0,1f))
        val footer=LinearLayout(this).apply {orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setPadding(0,12.dp,0,0)}
        footer.addView(ScannerIconView(this,ScannerIconView.Kind.SHIELD,RetroUi.brown),LinearLayout.LayoutParams(17.dp,17.dp).apply {rightMargin=7.dp})
        footer.addView(RetroUi.text(this,"Offline · Tanpa penyimpanan hasil",11f,false,RetroUi.brown))
        RetroUi.add(page,footer)
        if(lastReport!=null) RetroUi.add(page,RetroUi.action(this,"Lihat hasil terakhir",RetroUi.cream,RetroUi.green){showResult()},10)
    }

    private fun showGenerator() {
        val page=newPage()
        RetroUi.add(page,RetroUi.action(this,"‹  Kembali",RetroUi.cream,RetroUi.green){showHome()},0,8)
        RetroUi.add(page,RetroUi.text(this,"Buat LJK",23f,true,RetroUi.green),0,4)
        RetroUi.add(page,RetroUi.text(this,"Pilih ukuran, jumlah soal, dan jawaban. PDF sesuai format scanner.",12f,false,RetroUi.brown),0,12)
        val panel=RetroUi.panel(this)
        val papers=listOf("A5","A4")
        val opts=listOf("A–D","A–E")
        fun field(label:String,items:List<String>):Spinner {
            RetroUi.add(panel,RetroUi.text(this,label,12f,true,RetroUi.green),5,1)
            return Spinner(this).also { spinner ->
                spinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,items)
                RetroUi.add(panel,spinner,0,2)
            }
        }
        val paper=field("Ukuran kertas",papers)
        val count=field("Jumlah soal",listOf("10","20","30"))
        val options=field("Pilihan jawaban",opts)
        var updating=false
        fun setCounts(p:String,preferred:Int) {
            val allowed=if(p=="A5") listOf(10,20,30) else listOf(30,40,50)
            val chosen=if(preferred in allowed) preferred else if(p=="A5") 20 else 40
            count.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,allowed.map { it.toString() })
            count.setSelection(allowed.indexOf(chosen))
        }
        val initial=specs[selectedFormatIndex]
        updating=true
        paper.setSelection(papers.indexOf(initial.paper))
        setCounts(initial.paper,initial.count)
        options.setSelection(if(initial.options=="ABCD") 0 else 1)
        updating=false
        paper.onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent:android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent:android.widget.AdapterView<*>?,view:View?,position:Int,id:Long) {
                if(updating)return
                val old=count.selectedItem?.toString()?.toIntOrNull() ?: 20
                updating=true;setCounts(papers[position],old);updating=false
            }
        }
        RetroUi.add(panel,RetroUi.text(this,"Judul ujian",12f,true,RetroUi.green),9,1)
        val title=EditText(this).apply {setSingleLine(true);hint="Contoh: Penilaian Harian Matematika";setText(pdfTitle);textSize=15f}
        RetroUi.add(panel,title)
        RetroUi.add(panel,RetroUi.text(this,"Nama sekolah (opsional)",12f,true,RetroUi.green),7,1)
        val school=EditText(this).apply {setSingleLine(true);hint="Nama sekolah";setText(pdfSchool);textSize=15f}
        RetroUi.add(panel,school)
        RetroUi.add(panel,RetroUi.action(this,"Unduh PDF LJK",RetroUi.orange){
            val p=paper.selectedItem.toString()
            val n=count.selectedItem.toString().toInt()
            val o=if(options.selectedItemPosition==0) "ABCD" else "ABCDE"
            val idx=specs.indexOfFirst { it.paper==p && it.count==n && it.options==o }
            if(idx<0){toast("Format belum tersedia.");return@action}
            pdfTitle=title.text.toString().trim().ifBlank { "LEMBAR JAWABAN" }
            pdfSchool=school.text.toString().trim()
            selectedFormatIndex=idx
            if(::mode.isInitialized) mode.setSelection(idx)
            currentLayout=specs[idx].asset
            val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type="application/pdf"
                putExtra(Intent.EXTRA_TITLE,"LJK-$p-$n-${if(o=="ABCD") "AD" else "AE"}.pdf")
            }
            startActivityForResult(intent,pdfRequest)
        },10,5)
        RetroUi.add(panel,RetroUi.text(this,"Cetak skala 100% (Actual Size). Pastikan empat kotak sudut terlihat.",11f,false,RetroUi.brown))
        RetroUi.add(page,panel)
    }

    private fun showOptions() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Opsi Pemindaian")
            .setItems(arrayOf("Foto dengan kamera bawaan", "Petunjuk pemindaian")) { _, index ->
                if(index == 0) openCamera() else android.app.AlertDialog.Builder(this)
                    .setTitle("Panduan LJK")
                    .setMessage("Gunakan cahaya merata. Seluruh lembar dan empat kotak hitam harus terlihat. Hasil yang meragukan perlu diperiksa guru.")
                    .setPositiveButton("Mengerti", null).show()
            }.show()
    }

    /** v1.6.9: compact results, explicit uncertainty, and optional photo inspection. */
    private fun showResult() {
        val page = newPage()
        val back = RetroUi.text(this, "‹  Beranda", 15f, true, RetroUi.green).apply {
            setPadding(0, 4.dp, 0, 10.dp)
            setOnClickListener { showHome() }
        }
        RetroUi.add(page, back)
        RetroUi.add(page, RetroUi.text(this, "Hasil Pemindaian", 21f, true, RetroUi.green), 0, 12)

        val unanswered = lastAnswers.count { it == "-" || it == "–" }
        val unclear = lastAnswers.count { it == "?" }
        val marked = lastAnswers.size - unanswered - unclear
        val codeUnclear = lastCode.contains('?') || lastCode.isBlank()
        val needsReview = codeUnclear || unclear > 0 || lastWarnings.isNotEmpty()

        val info = RetroUi.panel(this)
        RetroUi.add(info, RetroUi.text(this, "KODE PESERTA", 11f, true, RetroUi.brown))
        RetroUi.add(info, RetroUi.text(this, lastCode.ifBlank { "???" }, 31f, true, RetroUi.green), 2)
        RetroUi.add(info, RetroUi.text(this, lastFormat, 12f, false, RetroUi.brown), 0, 9)
        RetroUi.add(info, RetroUi.text(this,
            if (needsReview) "PERLU PEMERIKSAAN MANUAL" else "HASIL TERBACA · TETAP PERIKSA LJK ASLI",
            12f, true, if (needsReview) RetroUi.orange else RetroUi.green))
        RetroUi.add(page, info, 0, 12)

        val summary = RetroUi.panel(this, RetroUi.greenLight)
        RetroUi.add(summary, RetroUi.text(this, "RINGKASAN ${lastAnswers.size} SOAL", 12f, true, RetroUi.green), 0, 7)
        RetroUi.add(summary, RetroUi.text(this,
            "Terisi: $marked     Kosong: $unanswered     Tidak jelas: $unclear",
            13f, true, RetroUi.green))
        RetroUi.add(page, summary, 0, 12)
        val answerKey=loadKey()
        if(answerKey.any { it.isNotEmpty() }) {
            val graded=RetroUi.panel(this,RetroUi.greenLight)
            val correct=lastAnswers.indices.count { i -> i < answerKey.size && answerKey[i].isNotEmpty() && lastAnswers[i]==answerKey[i] }
            val missingKey=answerKey.count { it.isEmpty() }
            val review=lastAnswers.count { it=="?" }
            val wrong=lastAnswers.indices.count { i -> i < answerKey.size && answerKey[i].isNotEmpty() && lastAnswers[i] != answerKey[i] && lastAnswers[i] != "?" && lastAnswers[i] != "-" && lastAnswers[i] != "–" }
            RetroUi.add(graded,RetroUi.text(this,"KOREKSI OTOMATIS",13f,true,RetroUi.green),0,7)
            RetroUi.add(graded,RetroUi.text(this,"Benar: $correct   Salah: $wrong   Kosong: $unanswered",14f,true,RetroUi.green),0,6)
            if(missingKey==0 && review==0 && !codeUnclear) {
                val score=100.0*correct/answerKey.size
                RetroUi.add(graded,RetroUi.text(this,"Nilai: %.1f / 100".format(java.util.Locale.US,score),24f,true,RetroUi.green),0,5)
            } else {
                RetroUi.add(graded,RetroUi.text(this,"Nilai final ditahan: $missingKey kunci kosong, $review jawaban tidak jelas${if(codeUnclear) ", kode peserta meragukan" else ""}.",12f,true,RetroUi.orange),0,5)
            }
            RetroUi.add(graded,RetroUi.text(this,"Skor sementara berbobot sama. Periksa LJK asli sebelum menetapkan nilai.",11f,false,RetroUi.brown))
            RetroUi.add(page,graded,0,12)
        } else {
            RetroUi.add(page,RetroUi.action(this,"Atur Kunci Jawaban untuk Koreksi",RetroUi.greenLight,RetroUi.green){showKeyEditor()},0,12)
        }

        if (needsReview) {
            val warning = RetroUi.panel(this)
            RetroUi.add(warning, RetroUi.text(this, "PERIKSA MANUAL", 13f, true, RetroUi.orange), 0, 7)
            val details = buildList {
                if (codeUnclear) add("Kode peserta belum terbaca jelas")
                if (unclear > 0) add("Nomor tidak jelas: " + lastAnswers.indices.filter { lastAnswers[it] == "?" }.joinToString(", ") { (it + 1).toString() })
                addAll(lastWarnings)
            }
            RetroUi.add(warning, RetroUi.text(this, details.joinToString("\n"), 12f, false, RetroUi.brown))
            RetroUi.add(page, warning, 0, 12)
        }

        val answersPanel = RetroUi.panel(this)
        RetroUi.add(answersPanel, RetroUi.text(this, "JAWABAN TERDETEKSI", 13f, true, RetroUi.green), 0, 10)
        val mid = (lastAnswers.size + 1) / 2
        val columns = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        for (range in listOf(0 until mid, mid until lastAnswers.size)) {
            val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            range.forEach { i ->
                val answer = lastAnswers[i]
                val row = RetroUi.text(this, "%02d     %s".format(i + 1, answer), 14f, true,
                    if (answer == "?") RetroUi.orange else RetroUi.ink).apply {
                    setPadding(9.dp, 7.dp, 0, 7.dp)
                    if (i % 2 == 0) setBackgroundColor(RetroUi.greenLight)
                }
                RetroUi.add(column, row, 1)
            }
            columns.addView(column, LinearLayout.LayoutParams(0, -2, 1f))
        }
        RetroUi.add(answersPanel, columns)
        RetroUi.add(page, answersPanel, 0, 12)

        RetroUi.add(page, RetroUi.action(this, "SCAN BERIKUTNYA", RetroUi.orange) { openLive() }, 0, 8)
        RetroUi.add(page, RetroUi.action(this, "Pilih Foto Lain", RetroUi.cream, RetroUi.green) { openGallery() }, 0, 12)

        lastImage?.let { image ->
            val toggle = RetroUi.action(this, "Lihat Foto LJK & Overlay", RetroUi.cream, RetroUi.green) { }
            val photo = RetroUi.panel(this).apply { visibility = View.GONE }
            RetroUi.add(photo, ImageView(this).apply {
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageBitmap(image)
            })
            toggle.setOnClickListener {
                val opening = photo.visibility != View.VISIBLE
                photo.visibility = if (opening) View.VISIBLE else View.GONE
                toggle.text = if (opening) "Sembunyikan Foto LJK" else "Lihat Foto LJK & Overlay"
            }
            RetroUi.add(page, toggle, 0, 8)
            RetroUi.add(page, photo, 0, 8)
        }
        RetroUi.add(page, RetroUi.text(this,
            "– = kosong  ·  ? = tidak jelas / ganda\nHasil perlu dicocokkan dengan LJK asli. Tidak ada penyimpanan hasil.",
            11f, false, RetroUi.brown), 12)
    }

    private fun selectedLayout(): String = specs[selectedFormatIndex].asset
    private var currentLayout = "scan-A5-20-ABCD.json"
    private var selectedFormatIndex = 2
    private fun openLive() {
        currentLayout = selectedLayoutSafely()
        startActivityForResult(Intent(this, LiveScannerActivity::class.java).putExtra("layout", currentLayout), liveRequest)
    }
    private fun selectedLayoutSafely(): String {
        if(::mode.isInitialized) {
            selectedFormatIndex = mode.selectedItemPosition.coerceIn(0, specs.lastIndex)
            currentLayout = selectedLayout()
        }
        return currentLayout
    }
    private fun openGallery() {
        currentLayout = selectedLayoutSafely()
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }, galleryRequest)
    }
    private fun openCamera() {
        currentLayout = selectedLayoutSafely()
        try {
            val file = File(cacheDir, "ljk-capture.jpg")
            photoUri = FileProvider.getUriForFile(this, "$packageName.files", file)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            val handlers = packageManager.queryIntentActivities(intent, 0)
            for(handler in handlers) grantUriPermission(handler.activityInfo.packageName, photoUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, cameraRequest)
        } catch(e: Exception) { toast("Kamera tidak dapat dibuka: ${e.message}") }
    }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    @Deprecated("Activity result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == pdfRequest) {
            if(resultCode == RESULT_OK && data?.data != null) {
                try {
                    val uri=data.data!!
                    val layout=JSONObject(assets.open(currentLayout).bufferedReader().use { it.readText() })
                    contentResolver.openOutputStream(uri)?.use { output ->
                        LjkGenerator(layout).write(output,pdfTitle,pdfSchool)
                    } ?: throw IllegalStateException("Tidak dapat membuka lokasi penyimpanan")
                    toast("PDF LJK berhasil disimpan. Cetak ukuran 100%.")
                    showHome()
                } catch(e:Exception) {toast("Gagal menyimpan PDF: ${e.message}")}
            }
            return
        }
        if(resultCode != RESULT_OK || isReading) return
        val uri = when(requestCode) {
            cameraRequest -> photoUri
            galleryRequest, liveRequest -> data?.data
            else -> null
        } ?: return
        isReading = true
        toast("Menganalisis foto LJK…")
        val asset = currentLayout
        Thread {
            try {
                val layout = assets.open(asset).bufferedReader().use { JSONObject(it.readText()) }
                val bitmap = contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
                    ?: error("Foto tidak terbaca")
                val orientation = contentResolver.openInputStream(uri).use { stream ->
                    if(stream == null) ExifInterface.ORIENTATION_NORMAL
                    else ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                }
                val rotation = when(orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
                val upright = if(rotation == 0f) bitmap else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height,
                    Matrix().apply { postRotate(rotation) }, true)
                val result = OmrReader(layout).read(upright)
                val warnings = result.qualityNotes + result.warnings
                runOnUiThread {
                    if(scanningKey) {
                        scanningKey=false
                        isReading=false
                        showScannedKey(result.answers)
                        return@runOnUiThread
                    }
                    lastImage = result.overlay
                    lastCode = result.code
                    lastFormat = layout.getString("format_id")
                    lastAnswers = result.answers
                    lastWarnings = warnings
                    lastReport = "OK"
                    isReading = false
                    showResult()
                }
            } catch(e: Exception) {
                runOnUiThread { isReading = false; scanningKey=false; toast("Pemindaian belum berhasil: ${e.message}") }
            }
        }.start()
    }

    @Deprecated("Back navigation")
    override fun onBackPressed() { showHome() }
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
