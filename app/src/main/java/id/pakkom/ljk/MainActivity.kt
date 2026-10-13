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
import java.io.File

/** v1.6.7: balanced homepage; OMR algorithm and JSON assets unchanged. */
class MainActivity : ComponentActivity() {
    private lateinit var mode: Spinner
    private lateinit var root: LinearLayout
    private var photoUri: Uri? = null
    private val cameraRequest = 101
    private val liveRequest = 103
    private val galleryRequest = 102
    private var lastImage: Bitmap? = null
    private var lastReport: String? = null
    private var lastCode: String = ""
    private var lastFormat: String = ""
    private var lastAnswers: List<String> = emptyList()
    private var lastWarnings: List<String> = emptyList()
    private var isReading = false

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

    /** v1.6.7: balanced, responsive homepage with consistent native vector icons. */
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
        RetroUi.add(identity,RetroUi.text(this,"SMART SCANNER  ·  v1.6.7",10f,true,RetroUi.brown),2)
        header.addView(identity,LinearLayout.LayoutParams(0,-2,1f))
        val settings=LinearLayout(this).apply {
            gravity=Gravity.CENTER;isClickable=true;isFocusable=true
            contentDescription="Pengaturan dan bantuan"
            addView(ScannerIconView(this@MainActivity,ScannerIconView.Kind.SETTINGS,RetroUi.green),LinearLayout.LayoutParams(25.dp,25.dp))
            setOnClickListener {showOptions()}
        }
        header.addView(settings,LinearLayout.LayoutParams(44.dp,44.dp))
        RetroUi.add(page,header,0,19)

        val formatRow=LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
            background=RetroUi.background(RetroUi.cream,13.dp.toFloat())
            setPadding(13.dp,5.dp,7.dp,5.dp)
        }
        val formatInfo=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL}
        RetroUi.add(formatInfo,RetroUi.text(this,"FORMAT AKTIF",10f,true,RetroUi.brown))
        formatRow.addView(formatInfo,LinearLayout.LayoutParams(-2,-2).apply {rightMargin=8.dp})
        mode=Spinner(this).apply {
            adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,
                listOf("A5 · 20 Soal · A–D","A5 · 30 Soal · A–E","A4 · 50 Soal · A–E"))
            setSelection(selectedFormatIndex);minimumHeight=43.dp
        }
        formatRow.addView(mode,LinearLayout.LayoutParams(0,-2,1f))
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

    private fun showResult() {
        val page = newPage()
        val back = RetroUi.text(this, "‹  Beranda", 16f, true, RetroUi.green).apply {
            setPadding(0, 4.dp, 0, 12.dp)
            setOnClickListener { showHome() }
        }
        RetroUi.add(page, back)
        RetroUi.add(page, RetroUi.text(this, "Hasil Pemindaian", 21f, true, RetroUi.green), 0, 14)

        val info = RetroUi.panel(this)
        RetroUi.add(info, RetroUi.text(this, "KODE PESERTA", 12f, true, RetroUi.brown))
        RetroUi.add(info, RetroUi.text(this, lastCode, 31f, true, RetroUi.green), 2)
        RetroUi.add(info, RetroUi.text(this, lastFormat, 13f, true, RetroUi.brown), 0, 8)
        RetroUi.add(info, RetroUi.text(this, "✓  LJK terdeteksi · Perspektif tervalidasi", 13f, true, RetroUi.green))
        RetroUi.add(page, info, 0, 14)

        val answersPanel = RetroUi.panel(this)
        RetroUi.add(answersPanel, RetroUi.text(this, "JAWABAN TERDETEKSI", 14f, true, RetroUi.green), 0, 12)
        val mid = (lastAnswers.size + 1) / 2
        val columns = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        for (range in listOf(0 until mid, mid until lastAnswers.size)) {
            val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            range.forEach { i ->
                RetroUi.add(column, RetroUi.text(this, "%02d     %s".format(i + 1, lastAnswers[i]), 15f, true).apply {
                    setPadding(9.dp, 8.dp, 0, 8.dp)
                    if (i % 2 == 0) setBackgroundColor(RetroUi.greenLight)
                }, 1)
            }
            columns.addView(column, LinearLayout.LayoutParams(0, -2, 1f))
        }
        RetroUi.add(answersPanel, columns)
        RetroUi.add(page, answersPanel, 0, 14)

        lastImage?.let { image ->
            val photo = RetroUi.panel(this)
            RetroUi.add(photo, RetroUi.text(this, "FOTO LJK & OVERLAY", 13f, true, RetroUi.brown), 0, 8)
            RetroUi.add(photo, ImageView(this).apply {
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageBitmap(image)
            })
            RetroUi.add(page, photo, 0, 14)
        }
        if (lastWarnings.isNotEmpty()) {
            val warning = RetroUi.panel(this)
            RetroUi.add(warning, RetroUi.text(this, "PERIKSA MANUAL", 14f, true, RetroUi.orange))
            RetroUi.add(warning, RetroUi.text(this, lastWarnings.joinToString("\n"), 13f), 6)
            RetroUi.add(page, warning, 0, 14)
        }
        RetroUi.add(page, RetroUi.action(this, "↻   SCAN BERIKUTNYA", RetroUi.orange) { openLive() }, 2, 10)
        RetroUi.add(page, RetroUi.action(this, "Pilih Foto Lain", RetroUi.cream, RetroUi.green) { openGallery() })
        RetroUi.add(page, RetroUi.text(this, "– kosong   ·   ? tidak jelas / ganda\nPeriksa hasil dengan lembar asli. Tidak ada penyimpanan data.", 12f, false, RetroUi.brown), 16)
    }

    private fun selectedLayout(): String = when(mode.selectedItemPosition) {
        0 -> "scan-A5-20.json"
        1 -> "scan-A5-30.json"
        else -> "scan-A4-50.json"
    }
    private var currentLayout = "scan-A5-20.json"
    private var selectedFormatIndex = 0
    private fun openLive() {
        currentLayout = selectedLayoutSafely()
        startActivityForResult(Intent(this, LiveScannerActivity::class.java).putExtra("layout", currentLayout), liveRequest)
    }
    private fun selectedLayoutSafely(): String {
        if(::mode.isInitialized) {
            selectedFormatIndex = mode.selectedItemPosition.coerceIn(0, 2)
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
                runOnUiThread { isReading = false; toast("Pemindaian belum berhasil: ${e.message}") }
            }
        }.start()
    }

    @Deprecated("Back navigation")
    override fun onBackPressed() { showHome() }
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
