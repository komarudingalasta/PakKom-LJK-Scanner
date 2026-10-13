package id.pakkom.ljk
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View

/** Native vector icons: never depend on Unicode glyphs or installed fonts. */
class ScannerIconView(context: Context, private val kind: Kind, private val tint: Int): View(context) {
    enum class Kind { SCAN, CAMERA, GALLERY, SETTINGS, SHIELD }
    private val p=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=tint;style=Paint.Style.STROKE;strokeWidth=2.6f;strokeCap=Paint.Cap.ROUND;strokeJoin=Paint.Join.ROUND }
    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val side=minOf(width,height).toFloat();c.save();c.translate((width-side)/2f,(height-side)/2f);c.scale(side/32f,side/32f)
        when(kind) {
            Kind.SCAN -> {
                c.drawLine(4f,12f,4f,5f,p);c.drawLine(4f,5f,11f,5f,p)
                c.drawLine(21f,5f,28f,5f,p);c.drawLine(28f,5f,28f,12f,p)
                c.drawLine(4f,20f,4f,27f,p);c.drawLine(4f,27f,11f,27f,p)
                c.drawLine(21f,27f,28f,27f,p);c.drawLine(28f,27f,28f,20f,p)
                c.drawLine(8f,16f,24f,16f,p)
            }
            Kind.CAMERA -> { c.drawRoundRect(RectF(3f,10f,29f,26f),3f,3f,p);c.drawRect(11f,7f,21f,10f,p);c.drawCircle(16f,18f,5f,p) }
            Kind.GALLERY -> { c.drawRoundRect(RectF(4f,5f,28f,27f),2f,2f,p);c.drawCircle(11f,12f,2f,p)
                val path=Path().apply {moveTo(6f,23f);lineTo(13f,16f);lineTo(18f,20f);lineTo(22f,15f);lineTo(27f,21f)};c.drawPath(path,p) }
            Kind.SETTINGS -> { c.drawCircle(16f,16f,9f,p);c.drawCircle(16f,16f,3.5f,p)
                for(i in 0..7) {val a=i*Math.PI/4;c.drawLine((16+11*Math.cos(a)).toFloat(),(16+11*Math.sin(a)).toFloat(),(16+14*Math.cos(a)).toFloat(),(16+14*Math.sin(a)).toFloat(),p)} }
            Kind.SHIELD -> { val path=Path().apply {moveTo(16f,3f);lineTo(27f,8f);lineTo(26f,19f);quadTo(24f,26f,16f,29f);quadTo(8f,26f,6f,19f);lineTo(5f,8f);close()};c.drawPath(path,p)
                c.drawLine(11f,16f,15f,20f,p);c.drawLine(15f,20f,22f,12f,p) }
        };c.restore()
    }
}
