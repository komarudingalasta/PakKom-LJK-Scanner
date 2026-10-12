package id.pakkom.ljk

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import org.json.JSONObject
import java.io.OutputStream
import kotlin.math.roundToInt

/** Render from the same millimetre coordinate maps used by the future OMR scanner. */
class LjkPdf(private val layout: JSONObject) {
    private val mm = 72f / 25.4f
    private val paper = layout.getString("paper")
    private val w = layout.getDouble("width_mm").toFloat()
    private val h = layout.getDouble("height_mm").toFloat()
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 0.65f
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.FILL }

    private fun Canvas.text(value: String, x: Float, y: Float, points: Float = 8f, bold: Boolean = false) {
        textPaint.textSize = points
        textPaint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        drawText(value, x * mm, y * mm, textPaint)
    }
    private fun Canvas.bubble(x: Float, y: Float, radius: Float) {
        drawCircle(x * mm, y * mm, radius * mm, ink)
    }
    private fun Canvas.rule(x1: Float, y1: Float, x2: Float, y2: Float) {
        drawLine(x1 * mm, y1 * mm, x2 * mm, y2 * mm, ink)
    }
    fun write(output: OutputStream) {
        val doc = PdfDocument()
        try {
            val page = doc.startPage(PdfDocument.PageInfo.Builder((w * mm).roundToInt(), (h * mm).roundToInt(), 1).create())
            val c = page.canvas
            c.drawColor(Color.WHITE)
            val markers = layout.getJSONArray("marks_mm")
            for (i in 0 until markers.length()) {
                val m = markers.getJSONObject(i)
                val x = m.getDouble("x_mm").toFloat() * mm
                val y = m.getDouble("y_mm").toFloat() * mm
                val size = m.getDouble("size_mm").toFloat() * mm
                c.drawRect(x, y, x + size, y + size, fill)
            }
            val small = paper == "A5"
            c.text("PAKKOM LJK SCANNER", 19f, 14f, if (small) 10f else 14f, true)
            c.text("LEMBAR JAWABAN UNIVERSAL", 19f, 20f, if (small) 6.5f else 8f, true)
            c.text("$paper | 50 SOAL", w - 36f, 20f, 7f, true)
            c.rule(16f, 24f, w - 16f, 24f)
            c.text("Nama : ______________________________", 17f, 31f, if (small) 6.8f else 8f)
            c.text("Kelas : __________   Tanggal : __________", 17f, 38f, if (small) 6.8f else 8f)
            c.text("Paket soal :", 17f, 45f, 7f)
            val packs = layout.getJSONArray("package_bubbles")
            for (i in 0 until packs.length()) {
                val p = packs.getJSONObject(i)
                val x = p.getDouble("x_mm").toFloat()
                val y = p.getDouble("y_mm").toFloat()
                c.bubble(x, y, if (small) 1.6f else 1.9f)
                c.text(p.getString("option"), x + 3f, 45f, 7f)
            }
            c.text("KODE PESERTA (3 DIGIT)", 17f, 54f, if (small) 7f else 8f, true)
            val ids = layout.getJSONArray("participant_code_bubbles")
            for (i in 0 until ids.length()) {
                val p = ids.getJSONObject(i)
                val x = p.getDouble("x_mm").toFloat()
                val y = p.getDouble("y_mm").toFloat()
                val digit = p.getInt("digit")
                if (digit == 0) c.text(p.getInt("column").toString(), x - 2f, 59f, 7f)
                c.bubble(x, y, if (small) 1.4f else 1.65f)
                c.text(digit.toString(), x + 3f, y + 1.1f, if (small) 5.5f else 6f)
            }
            val instX = if (small) 78f else 91f
            c.text("PETUNJUK PENGISIAN", instX, 62f, if (small) 6f else 7f, true)
            val instructions = listOf(
                "1. Hitamkan 1 angka per kolom kode.",
                "2. Hitamkan 1 pilihan tiap nomor.",
                "3. Gunakan pensil 2B / hitam pekat.",
                "4. Jangan lipat atau coret penanda."
            )
            instructions.forEachIndexed { i, line -> c.text(line, instX, 69f + i * 7f, if (small) 5.6f else 7f) }
            val start = if (small) 109f else 119f
            val end = h - if (small) 19f else 24f
            val pitch = (end - start) / 25f
            val left = if (small) 12f else 17f
            val colw = (w - 2f * left - 5f) / 2f
            val answerRadius = if (small) 1.6f else 2.1f
            for (col in 0..1) {
                val bx = left + col * (colw + 5f)
                c.text("No.", bx, start - 5f, if (small) 5.8f else 7f, true)
                for ((j, choice) in "ABCDE".withIndex()) {
                    val x = bx + 17f + j * ((colw - 22f) / 4f)
                    c.text(choice.toString(), x - 1.5f, start - 5f, if (small) 5.8f else 7f, true)
                }
            }
            val answers = layout.getJSONArray("answer_bubbles")
            for (i in 0 until answers.length()) {
                val q = answers.getJSONObject(i)
                val n = q.getInt("question")
                val options = q.getJSONArray("bubbles")
                val first = options.getJSONObject(0)
                val y = first.getDouble("y_mm").toFloat()
                val bx = left + if (n > 25) (colw + 5f) else 0f
                c.text(n.toString().padStart(2, ' '), bx, y + 1f, if (small) 6f else 7f)
                for (j in 0 until options.length()) {
                    val opt = options.getJSONObject(j)
                    c.bubble(opt.getDouble("x_mm").toFloat(), opt.getDouble("y_mm").toFloat(), answerRadius)
                }
            }
            c.rule(w / 2f, h - 28f, w / 2f, start + 24f * pitch + 4f)
            c.text(layout.getString("template_id"), 17f, h - 15f, if (small) 6f else 7f)
            c.text("Cetak 100% / Actual size - Jangan fit to page", 17f, h - 10f, if (small) 5.5f else 6f)
            doc.finishPage(page)
            doc.writeTo(output)
        } finally { doc.close() }
    }
}
