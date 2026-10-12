package id.pakkom.ljk

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File

class MainActivity : Activity() {
    private lateinit var mode: Spinner
    private lateinit var status: TextView
    private var photoUri: Uri? = null
    private val cameraRequest=101
    private val galleryRequest=102
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(20.dp,22.dp,20.dp,20.dp);setBackgroundColor(Color.rgb(247,249,250)) }
        setContentView(ScrollView(this).apply { addView(root) })
        fun label(t:String,size:Float=15f)=TextView(this).apply {text=t;textSize=size;setTextColor(Color.rgb(22,55,63));setPadding(0,8.dp,0,8.dp);root.addView(this)}
        label("PakKom LJK Scanner",24f)
        label("v1.2 · Prototipe pembaca OMR offline",14f)
        label("Pilih format lembar")
        mode=Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("A5 · 20 soal · A–D","A5 · 30 soal · A–E","A4 · 50 soal · A–E"));root.addView(this) }
        label("Foto seluruh kertas dengan keempat kotak hitam sudut terlihat. Gunakan cahaya terang dan rata.",13f)
        root.addView(Button(this).apply {text="FOTO LJK";setOnClickListener { openCamera() }})
        root.addView(Button(this).apply {text="PILIH FOTO DARI GALERI";setOnClickListener {startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {addCategory(Intent.CATEGORY_OPENABLE);type="image/*"},galleryRequest)} })
        status=TextView(this).apply {text="Hasil pemindaian akan tampil di sini. Hasil yang meragukan harus diverifikasi guru.";textSize=15f;setTextColor(Color.DKGRAY);setPadding(0,18.dp,0,18.dp);root.addView(this)}
        label("Catatan: pengenalan kotak dan bulatan masih eksperimental. Belum ada penilaian otomatis atau penyimpanan hasil.",13f)
    }
    private fun openCamera(){
        try {
            val file=File(cacheDir,"ljk-capture.jpg")
            photoUri=FileProvider.getUriForFile(this,"$packageName.files",file)
            val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {putExtra(MediaStore.EXTRA_OUTPUT,photoUri);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)}
            startActivityForResult(intent,cameraRequest)
        }catch(e:Exception){status.text="Kamera tidak dapat dibuka: ${e.message}"}
    }
    @Deprecated("Activity result API")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(resultCode!=RESULT_OK)return
        val uri=when(requestCode){cameraRequest->photoUri;galleryRequest->data?.data;else->null}?:return
        status.text="Menganalisis foto..."
        val asset=when(mode.selectedItemPosition){0->"scan-A5-20.json";1->"scan-A5-30.json";else->"scan-A4-50.json"}
        Thread {
            try {
                val layout=assets.open(asset).bufferedReader().use {JSONObject(it.readText())}
                val bitmap=contentResolver.openInputStream(uri).use {BitmapFactory.decodeStream(it)} ?:error("Foto tidak terbaca")
                val result=OmrReader(layout).read(bitmap)
                val text=buildString {
                    append("FORMAT: ${layout.getString("format_id")}\nKODE PESERTA: ${result.code}\n\n")
                    result.answers.forEachIndexed {i,v->append("${i+1}. $v    ");if((i+1)%4==0)append("\n")}
                    append("\n\nKeterangan: - = kosong, ? = tidak jelas / ganda\n")
                    if(result.warnings.isNotEmpty())append("\nPERIKSA MANUAL:\n"+result.warnings.joinToString("\n"))
                    else append("\nTidak ada peringatan otomatis. Tetap cocokkan dengan lembar asli.")
                }
                runOnUiThread {status.text=text}
            }catch(e:Exception){runOnUiThread {status.text="Pemindaian belum berhasil: ${e.message}"}}
        }.start()
    }
    private val Int.dp:Int get()=(this*resources.displayMetrics.density).toInt()
}
