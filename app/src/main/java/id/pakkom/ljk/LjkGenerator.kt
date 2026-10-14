package id.pakkom.ljk

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import org.json.JSONObject
import java.io.OutputStream
import kotlin.math.roundToInt

/** Generates supported layouts using their matching OMR JSON coordinates.
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
            c.label("Nama: ................................................",17f,headY+8f,if(compact)7.5f else 9f)
            c.label("Kelas: ...................",17f,headY+14f,if(compact)7.5f else 9f)
            // Portrait identity: tens ABOVE units, rombel in three rows of four.
            // Coordinates are loaded from the SAME scan JSON consumed by OmrReader.
            val identity=layout.getJSONObject("identity_bubbles")
            val border=Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color=Color.rgb(130,130,130); style=Paint.Style.STROKE; strokeWidth=0.18f*mm
            }
            val left=if(compact) 15f else 24f
            val right=pw-left
            val boxTop=if(compact) 41f else 45f
            val boxBottom=if(compact) 85f else 99f
            c.drawRoundRect(left*mm,boxTop*mm,right*mm,boxBottom*mm,1.6f*mm,1.6f*mm,border)
            c.label("IDENTITAS PESERTA",left+3f,boxTop+4.2f,if(compact)6.5f else 9f,true)
            val sep1=if(compact) 60f else 86f
            val sep2=if(compact) 94f else 134f
            c.drawLine(sep1*mm,(boxTop+7f)*mm,sep1*mm,(boxBottom-2f)*mm,border)
            c.drawLine(sep2*mm,(boxTop+7f)*mm,sep2*mm,(boxBottom-2f)*mm,border)
            c.label("TINGKAT KELAS",if(compact) 21f else 32f,boxTop+9f,if(compact)5.5f else 8f,true)
            c.label("ROMBEL",if(compact) 66f else 94f,boxTop+9f,if(compact)5.5f else 8f,true)
            c.label("NOMOR ABSEN",if(compact) 99f else 140f,boxTop+9f,if(compact)5.5f else 8f,true)
            c.label("Puluhan",if(compact) 20f else 31f,if(compact)50f else 55f,if(compact)4.4f else 7f)
            c.label("Satuan",if(compact) 20f else 31f,if(compact)63f else 74f,if(compact)4.4f else 7f)
            c.label("Puluhan",if(compact) 98f else 140f,if(compact)50f else 55f,if(compact)4.4f else 7f)
            c.label("Satuan",if(compact) 98f else 140f,if(compact)63f else 74f,if(compact)4.4f else 7f)
            val fields=listOf("grade_tens","grade_units","room","attendance_tens","attendance_units")
            for(key in fields) {
                val field=identity.getJSONObject(key)
                for(choice in field.keys()) {
                    val xy=field.getJSONArray(choice)
                    val cx=xy.getDouble(0).toFloat(); val cy=xy.getDouble(1).toFloat()
                    c.circle(cx,cy,if(compact)2.25f else 3f)
                    c.label(choice,cx,cy+0.75f,if(compact)4.5f else 6f,false,true)
                }
            }
            val answers=layout.getJSONObject("answers")
            val choices=layout.getString("options")
            val bubbleRadius=if(compact) (if(n==30) 2.35f else 2.5f) else (if(n==50) 2.65f else 3.0f)
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
