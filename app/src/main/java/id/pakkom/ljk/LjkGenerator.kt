package id.pakkom.ljk

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import org.json.JSONObject
import java.io.OutputStream
import kotlin.math.roundToInt

/** Generates only the three VERIFIED layouts bundled with the scanner.
 * Every mark and bubble is rendered from the exact JSON read by OmrReader.
 * PDF points are 72/25.4 per mm; print at 100% / actual size.
 */
class LjkGenerator(private val layout: JSONObject) {
    private val mm = 72f / 25.4f
    private val pw = layout.getJSONArray("paper_mm").getDouble(0).toFloat()
    private val ph = layout.getJSONArray("paper_mm").getDouble(1).toFloat()
    private val black = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 0.34f * mm }
    private val letters = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.FILL }
    private fun android.graphics.Canvas.label(value:String, x:Float, y:Float, pt:Float=8f, bold:Boolean=false, center:Boolean=false) {
        letters.textSize=pt; letters.typeface=if(bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        letters.textAlign=if(center) Paint.Align.CENTER else Paint.Align.LEFT
        drawText(value,x*mm,y*mm,letters)
    }
    private fun android.graphics.Canvas.circle(x:Float,y:Float,r:Float) { drawCircle(x*mm,y*mm,r*mm,line) }
    private fun textSafe(raw:String,max:Int=64)=raw.trim().replace(Regex("[\\r\\n\\t]+")," ").take(max)
    fun write(output:OutputStream,title:String,school:String="") {
        val doc=PdfDocument()
        try {
            val page=doc.startPage(PdfDocument.PageInfo.Builder((pw*mm).roundToInt(),(ph*mm).roundToInt(),1).create())
            val c=page.canvas;c.drawColor(Color.WHITE)
            val marks=layout.getJSONArray("registration_marks_mm")
            // OMR registration coordinates are marker CENTERS, not top-lefts.
            for(i in 0 until marks.length()) {
                val pos=marks.getJSONArray(i)
                val x=pos.getDouble(0).toFloat()*mm;val y=pos.getDouble(1).toFloat()*mm
                val half=3.0f*mm
                c.drawRect(x-half,y-half,x+half,y+half,black)
            }
            val compact=layout.getString("paper")=="A5"
            val n=layout.getInt("question_count")
            val headY=if(compact && n==30) 19f else 22f
            val t=textSafe(title).ifBlank { "LEMBAR JAWABAN" }
            val titleSize=if(compact) 10f else 13f
            // Fit title to one centered line, without altering any bubble geometry.
            var pt=titleSize
            letters.typeface=Typeface.DEFAULT_BOLD
            while(pt>7f) {letters.textSize=pt;if(letters.measureText(t)<= (pw-32f)*mm)break;pt-=0.5f}
            c.label(t,pw/2f,headY,pt,true,true)
            val s=textSafe(school,52)
            if(s.isNotBlank()) c.label(s,pw/2f,headY-7f,if(compact)7f else 8.5f,false,true)
            c.label("Nama: ................................................",17f,headY+10f,if(compact)7.5f else 9f)
            c.label("Kelas: ...................",17f,headY+18f,if(compact)7.5f else 9f)
            val ids=layout.getJSONObject("participant_code")
            val first=ids.getJSONObject("1").getJSONArray("0")
            val idY=first.getDouble(1).toFloat()
            c.label("KODE PESERTA (3 DIGIT)",17f,idY-8f,if(compact)7.5f else 9f,true)
            for(d in 1..3) {
                val row=ids.getJSONObject(d.toString())
                for(v in 0..9) {
                    val xy=row.getJSONArray(v.toString())
                    val x=xy.getDouble(0).toFloat();val y=xy.getDouble(1).toFloat()
                    c.circle(x,y,if(compact)2.2f else 2.5f)
                    // Digits appear INSIDE the bubble, keeping print minimal.
                    c.label(v.toString(),x,y+0.9f,if(compact)5.5f else 6.5f,false,true)
                }
            }
            val answers=layout.getJSONObject("answers")
            val choices=layout.getString("options")
            val bubbleRadius=if(compact) 2.5f else 3.0f
            for(q in 1..n) {
                val row=answers.getJSONObject(q.toString())
                val firstOption=row.getJSONArray(choices[0].toString())
                val x=firstOption.getDouble(0).toFloat();val y=firstOption.getDouble(1).toFloat()
                c.label(q.toString().padStart(2,'0'),x-(if(compact) 13f else 16f),y+1.1f,if(compact)7f else 9f)
                for(option in choices) {
                    val xy=row.getJSONArray(option.toString())
                    val cx=xy.getDouble(0).toFloat();val cy=xy.getDouble(1).toFloat()
                    c.circle(cx,cy,bubbleRadius)
                    c.label(option.toString(),cx,cy+1f,if(compact)5.5f else 6.5f,false,true)
                }
            }
            c.label(layout.getString("format_id"),17f,ph-12f,if(compact)5.5f else 6.5f)
            c.label("CETAK 100% (ACTUAL SIZE)",17f,ph-8f,if(compact)5.5f else 6.5f)
            doc.finishPage(page);doc.writeTo(output)
        } finally { doc.close() }
    }
}
