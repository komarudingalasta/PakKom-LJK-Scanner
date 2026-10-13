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
import android.widget.*
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var mode: Spinner
    private lateinit var status: TextView
    private lateinit var preview: ImageView
    private var photoUri: Uri? = null
    private val cameraRequest=101
    private val liveRequest=103
    private val galleryRequest=102
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(20.dp,22.dp,20.dp,20.dp);setBackgroundColor(Color.rgb(247,249,250)) }
        setContentView(ScrollView(this).apply { addView(root) })
        fun label(t:String,size:Float=15f)=TextView(this).apply {text=t;textSize=size;setTextColor(Color.rgb(22,55,63));setPadding(0,8.dp,0,8.dp);root.addView(this)}
        label("PakKom LJK Scanner",24f)
        label("v1.6.0 · Live Camera · Auto Capture (eksperimental)",14f)
        label("Pilih format lembar")
        mode=Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("A5 · 20 soal · A–D","A5 · 30 soal · A–E","A4 · 50 soal · A–E"));root.addView(this) }
        label("Foto seluruh kertas dengan keempat kotak hitam terlihat. Uji posisi lurus, miring sekitar 10°, dan 20°. Gunakan cahaya rata.",13f)
        root.addView(Button(this).apply {text="BUKA LIVE SCANNER";setOnClickListener { startActivityForResult(Intent(this@MainActivity, LiveScannerActivity::class.java).putExtra("layout",selectedLayout()),liveRequest) }})
        root.addView(Button(this).apply {text="FOTO LJK (KAMERA BAWAAN)";setOnClickListener { openCamera() }})
        root.addView(Button(this).apply {text="PILIH FOTO DARI GALERI";setOnClickListener {startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {addCategory(Intent.CATEGORY_OPENABLE);type="image/*"},galleryRequest)} })
        root.addView(Button(this).apply {text="SCAN ULANG / BERSIHKAN HASIL";setOnClickListener {preview.setImageDrawable(null);status.text="Siap memindai LJK berikutnya. Pilih kamera atau galeri."} })
        preview=ImageView(this).apply { adjustViewBounds=true;scaleType=ImageView.ScaleType.FIT_CENTER;root.addView(this) }
        status=TextView(this).apply {text="Hasil pemindaian akan tampil di sini. Hasil yang meragukan harus diverifikasi guru.";textSize=15f;setTextColor(Color.DKGRAY);setPadding(0,18.dp,0,18.dp);root.addView(this)}
        label("Lingkaran hijau = terpilih; biru = tidak terpilih. Cocokkan overlay dengan lembar asli. Tidak ada penyimpanan atau penilaian.",13f)
    }
    private fun selectedLayout()=when(mode.selectedItemPosition){0->"scan-A5-20.json";1->"scan-A5-30.json";else->"scan-A4-50.json"}
    private fun openCamera(){
        try {
            val file=File(cacheDir,"ljk-capture.jpg")
            photoUri=FileProvider.getUriForFile(this,"$packageName.files",file)
            val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {putExtra(MediaStore.EXTRA_OUTPUT,photoUri);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)}
            val handlers=packageManager.queryIntentActivities(intent,0)
            for(handler in handlers)grantUriPermission(handler.activityInfo.packageName,photoUri,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent,cameraRequest)
        }catch(e:Exception){status.text="Kamera belum dapat dibuka. Coba kembali atau gunakan galeri. Detail: ${e.javaClass.simpleName}"}
    }
    @Deprecated("Activity result API")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(resultCode!=RESULT_OK)return
        val uri=when(requestCode){cameraRequest->photoUri;galleryRequest->data?.data;liveRequest->data?.data;else->null}?:return
        status.text="Menganalisis foto..."
        val asset=selectedLayout()
        Thread {
            try {
                val layout=assets.open(asset).bufferedReader().use {JSONObject(it.readText())}
                val bitmap=contentResolver.openInputStream(uri).use {BitmapFactory.decodeStream(it)} ?:error("Foto tidak terbaca")
                val orientation=contentResolver.openInputStream(uri).use { stream -> if(stream==null) ExifInterface.ORIENTATION_NORMAL else ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL) }
                val rotation=when(orientation){ExifInterface.ORIENTATION_ROTATE_90->90f;ExifInterface.ORIENTATION_ROTATE_180->180f;ExifInterface.ORIENTATION_ROTATE_270->270f;else->0f}
                val upright=if(rotation==0f)bitmap else Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,Matrix().apply{postRotate(rotation)},true)
                val result=OmrReader(layout).read(upright)
                val text=buildString {
                    append("FORMAT: ${layout.getString("format_id")}\nKODE PESERTA: ${result.code}\nSTATUS: LJK terdeteksi dan perspektif tervalidasi\n\n")
                    result.answers.forEachIndexed {i,v->append("${i+1}. $v    ");if((i+1)%4==0)append("\n")}
                    append("\n\nKeterangan: - = kosong, ? = tidak jelas / ganda\n")
                    if(result.qualityNotes.isNotEmpty())append("\nKUALITAS FOTO (SARAN):\n" + result.qualityNotes.joinToString("\n") + "\n")
                    else append("\nKUALITAS FOTO: tidak ditemukan masalah pencahayaan mencolok (bukan jaminan akurasi).\n")
                    if(result.warnings.isNotEmpty())append("\nPERIKSA MANUAL:\n"+result.warnings.joinToString("\n"))
                    else append("\nTidak ada peringatan otomatis. Tetap cocokkan dengan lembar asli.")
                }
                runOnUiThread {status.text=text;preview.setImageBitmap(result.overlay)}
            }catch(e:Exception){runOnUiThread {status.text="Pemindaian belum berhasil: ${e.message}"}}
        }.start()
    }
    private val Int.dp:Int get()=(this*resources.displayMetrics.density).toInt()
}
