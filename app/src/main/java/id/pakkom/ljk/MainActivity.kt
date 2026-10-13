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

/** v1.6.3: retro UI and result screen only. OMR algorithm and JSON assets unchanged. */
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
            setPadding(18.dp, 20.dp, 18.dp, 22.dp)
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; addView(page) }
        setContentView(scroll)
        root = page
        return page
    }

    private fun showHome() {
        val page = newPage()
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val title = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        RetroUi.add(title, RetroUi.text(this, "PakKom LJK", 29f, true, RetroUi.green))
        RetroUi.add(title, RetroUi.text(this, "SCANNER  /  RETRO MODERN", 11f, true, RetroUi.brown), 3)
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(RetroUi.text(this, "⚙", 27f, true, RetroUi.green).apply {
            setPadding(12.dp, 8.dp, 8.dp, 8.dp)
            contentDescription = "Pengaturan dan bantuan"
            setOnClickListener { showOptions() }
        })
        RetroUi.add(page, header, 4, 25)

        val formatPanel = RetroUi.panel(this)
        RetroUi.add(formatPanel, RetroUi.text(this, "FORMAT LEMBAR", 12f, true, RetroUi.brown), 0, 8)
        mode = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("A5  ·  20 soal  ·  A–D", "A5  ·  30 soal  ·  A–E", "A4  ·  50 soal  ·  A–E"))
            setSelection(selectedFormatIndex)
            minimumHeight = 48.dp
        }
        RetroUi.add(formatPanel, mode)
        RetroUi.add(page, formatPanel, 0, 16)

        val hero = RetroUi.panel(this, RetroUi.greenLight)
        val symbol = RetroUi.text(this, "▣", 56f, true, RetroUi.green).apply { gravity = Gravity.CENTER }
        RetroUi.add(hero, symbol, 8)
        RetroUi.add(hero, RetroUi.text(this, "Siap memindai?", 23f, true, RetroUi.green).apply {
            gravity = Gravity.CENTER
        }, 2)
        RetroUi.add(hero, RetroUi.text(this, "Arahkan kamera hingga keempat penanda sudut terlihat.", 13f, false, RetroUi.brown).apply {
            gravity = Gravity.CENTER
        }, 8, 18)
        RetroUi.add(hero, RetroUi.action(this, "◉   MULAI SCAN", RetroUi.orange) { openLive() })
        RetroUi.add(hero, RetroUi.action(this, "▧   Pilih dari Galeri", RetroUi.cream, RetroUi.green) { openGallery() }, 10)
        RetroUi.add(page, hero)

        RetroUi.add(page, RetroUi.text(this, "OFFLINE   ·   CEPAT   ·   TANPA PENYIMPANAN", 11f, true, RetroUi.brown).apply {
            gravity = Gravity.CENTER
        }, 24)
        if (lastReport != null) {
            RetroUi.add(page, RetroUi.action(this, "Lihat Hasil Terakhir  →", RetroUi.cream, RetroUi.green) { showResult() }, 16)
        }
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
        RetroUi.add(page, RetroUi.text(this, "Hasil Pemindaian", 25f, true, RetroUi.green), 0, 14)

        val info = RetroUi.panel(this)
        RetroUi.add(info, RetroUi.text(this, "KODE PESERTA", 12f, true, RetroUi.brown))
        RetroUi.add(info, RetroUi.text(this, lastCode, 38f, true, RetroUi.green), 2)
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
