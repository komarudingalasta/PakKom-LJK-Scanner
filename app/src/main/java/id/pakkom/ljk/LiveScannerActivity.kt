package id.pakkom.ljk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/** Live camera is additive. Original gallery/camera and OMR engine are untouched. */
class LiveScannerActivity : ComponentActivity() {
    private lateinit var view: PreviewView
    private lateinit var message: TextView
    private lateinit var capture: ImageCapture
    private val executor = Executors.newSingleThreadExecutor()
    private val busy = AtomicBoolean(false)
    private var auto = false
    private var lastCheck = 0L
    private var lastSignature = ""
    private var stableFrames = 0
    private val requiredStableFrames = 3
    private var lastAutoCaptureAt = 0L
    private val captureCooldownMs = 3500L
    private var taking = false
    private lateinit var layout: JSONObject
    private val requestPermission = 301

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val asset = intent.getStringExtra("layout") ?: "scan-A5-20.json"
        layout = assets.open(asset).bufferedReader().use { JSONObject(it.readText()) }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(RetroUi.paper) }
        val header = TextView(this).apply { text="‹   LIVE SCANNER  ·  ${this@LiveScannerActivity.layout.optString("format_id")}"; textSize=17f;setTypeface(null, 1);setTextColor(RetroUi.cream);setBackgroundColor(RetroUi.green);setPadding(18,22,18,18);setOnClickListener { finish() } }
        root.addView(header)
        val frame = FrameLayout(this)
        view = PreviewView(this).apply { scaleType = PreviewView.ScaleType.FIT_CENTER }
        frame.addView(view, FrameLayout.LayoutParams(-1,-1))
        val guide = ScanGuideView(this)
        frame.addView(guide,FrameLayout.LayoutParams(-1,-1))
        root.addView(frame,LinearLayout.LayoutParams(-1,0,1f))
        message = TextView(this).apply {text="Arahkan kamera ke seluruh LJK; empat kotak hitam harus terlihat.";setTextColor(RetroUi.ink);setBackgroundColor(RetroUi.cream);textSize=14f;setPadding(18,12,18,12)}
        root.addView(message)
        val buttons=LinearLayout(this).apply { setBackgroundColor(RetroUi.cream);setPadding(8,8,8,8) }
        val autoButton=Button(this).apply { text="AUTO OFF";setOnClickListener { auto=!auto; text=if(auto)"AUTO ON" else "AUTO OFF";stableFrames=0;lastSignature="";message.text=if(auto)"AUTO aktif: tahan kertas hingga hasil stabil." else "AUTO mati: tekan FOTO untuk memindai." } }
        autoButton.setTextColor(RetroUi.green)
        buttons.addView(autoButton,LinearLayout.LayoutParams(0,-2,1f))
        buttons.addView(Button(this).apply {text="● FOTO";setTextColor(RetroUi.green);setOnClickListener { takePhoto() }},LinearLayout.LayoutParams(0,-2,1f))
        buttons.addView(Button(this).apply {text="KEMBALI";setTextColor(RetroUi.brown);setOnClickListener {finish()}},LinearLayout.LayoutParams(0,-2,1f))
        root.addView(buttons)
        setContentView(root)
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) startCamera()
        else requestPermissions(arrayOf(Manifest.permission.CAMERA),requestPermission)
    }
    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<String>,grantResults:IntArray){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        if(requestCode==requestPermission){
            if(grantResults.firstOrNull()==PackageManager.PERMISSION_GRANTED)startCamera()
            else message.text="Izin kamera ditolak. Gunakan galeri atau kamera bawaan dari halaman utama."
        }
    }
    private fun startCamera(){
        val future=ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                val provider=future.get()
                val preview=Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
                capture=ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build()
                val analysis=ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
                analysis.setAnalyzer(executor) { frame -> inspect(frame) }
                provider.unbindAll()
                provider.bindToLifecycle(this,CameraSelector.DEFAULT_BACK_CAMERA,preview,capture,analysis)
            } catch(e:Exception) {message.text="Kamera gagal dibuka: ${e.message}"}
        },ContextCompat.getMainExecutor(this))
    }
    private fun inspect(frame:ImageProxy){
        try {
            val now=SystemClock.elapsedRealtime()
            if(!auto || taking || now-lastCheck<1700L || !busy.compareAndSet(false,true))return
            lastCheck=now
            try {
                val plane=frame.planes[0]
                val buffer=plane.buffer
                val w=frame.width;val h=frame.height
                val pixels=IntArray(w*h)
                val row=ByteArray(plane.rowStride)
                for(y in 0 until h){
                    buffer.position(y*plane.rowStride);buffer.get(row,0,minOf(plane.rowStride,buffer.remaining()))
                    for(x in 0 until w){val i=x*plane.pixelStride;val r=row[i].toInt() and 255;val g=row[i+1].toInt() and 255;val b=row[i+2].toInt() and 255;pixels[y*w+x]=Color.rgb(r,g,b)}
                }
                var bitmap=Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888)
                val rotation=frame.imageInfo.rotationDegrees
                if(rotation!=0) bitmap=Bitmap.createBitmap(bitmap,0,0,w,h,Matrix().apply {postRotate(rotation.toFloat())},true)
                // Analyze a downsized frame to limit CPU/memory. No result is accepted unless
                // four fiducials pass the existing OMR perspective validation.
                val maxSide=1000
                if(maxOf(bitmap.width,bitmap.height)>maxSide){val f=maxSide.toFloat()/maxOf(bitmap.width,bitmap.height);bitmap=Bitmap.createScaledBitmap(bitmap,(bitmap.width*f).toInt(),(bitmap.height*f).toInt(),true)}
                val result=OmrReader(layout).read(bitmap)
                val valid=result.qualityNotes.isEmpty() && result.code.none { it=='?' } && result.answers.none { it=="?" } && result.answers.any { it!="-" }
                val signature=result.code+"|"+result.answers.joinToString("")
                if(valid){stableFrames=if(signature==lastSignature)stableFrames+1 else 1;lastSignature=signature}
                else {stableFrames=0;lastSignature=""}
                runOnUiThread {if(!taking)message.text=if(valid)"LJK terdeteksi · stabil $stableFrames/$requiredStableFrames" else "LJK terlihat, tetapi hasil/kualitas belum cukup jelas. Atur cahaya atau tekan FOTO."}
                if(valid && stableFrames>=requiredStableFrames && now-lastAutoCaptureAt>=captureCooldownMs){
                    lastAutoCaptureAt=now
                    stableFrames=0
                    runOnUiThread { if(auto && !taking)takePhoto() }
                }
            }catch(e:Exception){stableFrames=0;lastSignature="";runOnUiThread {if(!taking)message.text="Cari keempat penanda sudut, lalu tahan kamera stabil..."}}
            finally {busy.set(false)}
        }finally{frame.close()}
    }
    private fun takePhoto(){
        if(taking || !::capture.isInitialized)return
        taking=true;auto=false;stableFrames=0;lastSignature="";message.text="Mengambil foto resolusi penuh..."
        val file=File(cacheDir,"live-ljk-${System.currentTimeMillis()}.jpg")
        capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(),ContextCompat.getMainExecutor(this),object:ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(output:ImageCapture.OutputFileResults){
                val uri=FileProvider.getUriForFile(this@LiveScannerActivity,"$packageName.files",file)
                setResult(RESULT_OK,Intent().setData(uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                finish()
            }
            override fun onError(exception:ImageCaptureException){taking=false;message.text="Gagal mengambil foto: ${exception.message}. Coba lagi."}
        })
    }
    override fun onDestroy(){super.onDestroy();executor.shutdown()}
}
