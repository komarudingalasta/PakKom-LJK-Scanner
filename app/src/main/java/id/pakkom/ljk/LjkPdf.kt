package id.pakkom.ljk

import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject
import java.io.OutputStream

/** All coordinates in millimeters from the A4 top-left corner. */
class LjkPdf(private val layout: JSONObject) {
    data class Student(val name: String, val nis: String, val classroom: String, val exam: String, val pack: String)
    private val width = 595 // A4 at 72 dpi, rounded
    private val height = 842
    private val mm = 72f / 25.4f
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 0.32f }
    private val txt = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.FILL; textSize = 9f }
    private fun Canvas.text(t: String, x: Float, y: Float, size: Float = 9f, bold: Boolean = false) {
        txt.textSize = size; txt.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        drawText(t, x * mm, y * mm, txt)
    }
    private fun Canvas.bubble(x: Float, y: Float, r: Float = 2.35f) {
        drawCircle(x * mm, y * mm, r * mm, ink)
    }
    private fun Canvas.lineMm(x1: Float, y1: Float, x2: Float, y2: Float) {
        drawLine(x1 * mm, y1 * mm, x2 * mm, y2 * mm, ink)
    }
    fun write(output: OutputStream, personal: Boolean, student: Student) {
        val doc = PdfDocument()
        try {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(width, height, 1).create())
            val c = page.canvas
            c.drawColor(Color.WHITE)
            val markers = layout.getJSONArray("markers")
            for (i in 0 until markers.length()) {
                val m = markers.getJSONObject(i)
                val x = m.getDouble("x").toFloat(); val y = m.getDouble("y").toFloat()
                val solid = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
                c.drawRect((x-3)*mm, (y-3)*mm, (x+3)*mm, (y+3)*mm, solid)
            }
            c.text("PAKKOM LJK SCANNER", 26f, 21f, 15f, true)
            c.text("Lembar Jawaban Komputer | 50 Soal | A-E", 26f, 28f, 8f)
            c.text("Kode: PK-LJK-50-AE-V1", 26f, 34f, 7f)
            c.lineMm(25f, 37f, 187f, 37f)
            c.text("Nama : ${if (personal) student.name else "........................................................"}", 25f, 43f)
            c.text("Kelas : ${if (personal) student.classroom else "....................."}", 25f, 49f)
            c.text("Ujian : ${student.exam}", 25f, 55f)
            if (personal) {
                c.text("NIS : ${student.nis}", 25f, 61f)
                c.text("Paket : ${student.pack}", 25f, 67f)
                drawQr(c, "PKLJK1|${student.nis}|${student.exam}|${student.pack}", 157f, 46f, 30f)
                c.text("Identitas tercetak otomatis", 25f, 77f, 7f)
            } else {
                c.text("NIS: hitamkan satu angka per kolom", 25f, 60f, 7f, true)
                val nis = layout.getJSONObject("generalNis").getJSONObject("choices")
                for (digit in 1..8) {
                    val col = nis.getJSONObject(digit.toString())
                    val x = col.getJSONObject("0").getDouble("x").toFloat()
                    c.text(digit.toString(), x-1f, 58f, 6f, true)
                    for (v in 0..9) {
                        val p = col.getJSONObject(v.toString())
                        c.bubble(p.getDouble("x").toFloat(), p.getDouble("y").toFloat(), 1.45f)
                    }
                }
                for (v in 0..9) c.text(v.toString(), 48f, 64f + 4.2f*v, 6f)
                c.text("Paket:", 25f, 107f, 7f)
                val packs = layout.getJSONObject("generalPackage")
                for (p in listOf("A", "B", "C", "D")) {
                    val pos = packs.getJSONObject(p)
                    c.bubble(pos.getDouble("x").toFloat(), pos.getDouble("y").toFloat(), 1.65f)
                    c.text(p, pos.getDouble("x").toFloat()+3f, 107f, 7f)
                }
            }
            c.lineMm(25f, 112f, 187f, 112f)
            val questions = layout.getJSONArray("questions")
            for (i in 0 until questions.length()) {
                val q = questions.getJSONObject(i)
                val opts = q.getJSONObject("choices")
                val first = opts.getJSONObject("A")
                val y = first.getDouble("y").toFloat()
                val x = first.getDouble("x").toFloat()
                c.text("${i+1}.", x-15f, y+1.25f, 7f)
                for (choice in listOf("A", "B", "C", "D", "E")) {
                    val point = opts.getJSONObject(choice)
                    c.bubble(point.getDouble("x").toFloat(), point.getDouble("y").toFloat())
                }
            }
            c.text("Isi bulatan penuh dengan pensil hitam. Cetak pada ukuran 100%.", 25f, 277f, 7f)
            c.text("Jangan lipat, coret penanda sudut, atau mengubah skala cetak.", 25f, 281f, 7f)
            doc.finishPage(page)
            doc.writeTo(output)
        } finally { doc.close() }
    }
    private fun drawQr(canvas: Canvas, value: String, x: Float, y: Float, size: Float) {
        val bits = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 144, 144, mapOf(EncodeHintType.MARGIN to 1))
        val cell = size * mm / bits.width
        val p = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
        for (row in 0 until bits.height) for (col in 0 until bits.width) {
            if (bits.get(col,row)) canvas.drawRect(x*mm+col*cell, y*mm+row*cell, x*mm+(col+1)*cell, y*mm+(row+1)*cell, p)
        }
    }
}
