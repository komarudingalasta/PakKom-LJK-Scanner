package id.pakkom.ljk
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/** Static LJK positioning guide; not part of camera or OMR processing. */
class LjkGuideView(context: Context):View(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onMeasure(w:Int,h:Int) {setMeasuredDimension(MeasureSpec.getSize(w),(136*resources.displayMetrics.density).toInt())}
    override fun onDraw(c:Canvas) {
        super.onDraw(c);c.save();c.scale(width/320f,height/145f)
        p.style=Paint.Style.FILL;p.color=0xFFF1F4F0.toInt();c.drawRoundRect(RectF(0f,0f,320f,145f),10f,10f,p)
        p.color=0xFFFFFFFF.toInt();c.drawRoundRect(RectF(100f,6f,220f,139f),4f,4f,p)
        p.style=Paint.Style.STROKE;p.strokeWidth=1.5f;p.color=0xFFB0C2BA.toInt();c.drawRoundRect(RectF(100f,6f,220f,139f),4f,4f,p)
        p.style=Paint.Style.FILL;p.color=RetroUi.green
        for(x in listOf(107f,207f)) for(y in listOf(13f,126f)) c.drawRect(x,y,x+7,y+7,p)
        p.color=0xFFD3DFD9.toInt();c.drawRoundRect(RectF(119f,29f,199f,34f),2f,2f,p);c.drawRoundRect(RectF(119f,40f,176f,44f),2f,2f,p)
        for(r in 0..4) for(col in 0..3) {p.style=Paint.Style.STROKE;p.strokeWidth=1.5f;p.color=0xFF93B3A8.toInt();c.drawCircle(130f+col*16f,63f+r*12f,4f,p)}
        p.style=Paint.Style.FILL;p.color=RetroUi.orange;c.drawCircle(162f,75f,3.2f,p)
        p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=RetroUi.orange
        val lines=arrayOf(floatArrayOf(81f,24f,81f,9f,96f,9f),floatArrayOf(224f,9f,239f,9f,239f,24f),floatArrayOf(81f,121f,81f,136f,96f,136f),floatArrayOf(224f,136f,239f,136f,239f,121f))
        for(a in lines){c.drawLine(a[0],a[1],a[2],a[3],p);c.drawLine(a[2],a[3],a[4],a[5],p)}
        c.restore()
    }
}
