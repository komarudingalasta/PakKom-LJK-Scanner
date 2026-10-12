package id.pakkom.ljk

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import org.json.JSONObject
import kotlin.math.*

/** Prototype: printed registration marks must be visible; all coordinates in millimetres. */
class OmrReader(private val layout: JSONObject) {
    data class Result(val code: String, val answers: List<String>, val warnings: List<String>, val image: Bitmap, val overlay: Bitmap)
    private val widthMm = layout.getJSONArray("paper_mm").getDouble(0)
    private val heightMm = layout.getJSONArray("paper_mm").getDouble(1)
    private val scale = 5.0 // px/mm in normalized bitmap
    private fun gray(pixel: Int): Int = (Color.red(pixel)*30 + Color.green(pixel)*59 + Color.blue(pixel)*11)/100
    private fun coords(array: org.json.JSONArray): Pair<Double,Double> = array.getDouble(0) to array.getDouble(1)

    fun read(input: Bitmap): Result {
        // Downscale large photos to cap memory; preserve aspect ratio.
        val factor = min(1.0, 1800.0/max(input.width,input.height))
        val photo = if(factor<1) Bitmap.createScaledBitmap(input,(input.width*factor).toInt(),(input.height*factor).toInt(),true) else input
        val marks = findMarks(photo) ?: throw IllegalArgumentException("Empat kotak sudut tidak terdeteksi. Foto seluruh kertas, gunakan cahaya rata, dan hindari bayangan.")
        val reg = layout.getJSONArray("registration_marks_mm")
        val source = FloatArray(8)
        val target = FloatArray(8)
        for (i in 0..3) {
            source[2*i]=marks[i].first.toFloat();source[2*i+1]=marks[i].second.toFloat()
            val (x,y)=coords(reg.getJSONArray(i));target[2*i]=(x*scale).toFloat();target[2*i+1]=(y*scale).toFloat()
        }
        val transform=Matrix()
        if(!transform.setPolyToPoly(source,0,target,0,4)) throw IllegalArgumentException("Perspektif foto tidak dapat diperbaiki")
        val normalized=Bitmap.createBitmap((widthMm*scale).toInt(),(heightMm*scale).toInt(),Bitmap.Config.ARGB_8888)
        val canvas=android.graphics.Canvas(normalized);canvas.drawColor(Color.WHITE);canvas.drawBitmap(photo,transform,android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG))
        val warnings=mutableListOf<String>()
        val digits=layout.getJSONObject("participant_code")
        val code=StringBuilder()
        for (d in 1..3) {
            val row=digits.getJSONObject(d.toString())
            val scores=(0..9).map { i -> fillScore(normalized,coords(row.getJSONArray(i.toString())),1.75) }
            val selected=select(scores)
            code.append(if(selected<0) '?' else ('0'+selected))
            if(selected<0)warnings.add("Kode peserta digit $d kosong, ganda, atau tidak jelas")
        }
        val answerJson=layout.getJSONObject("answers")
        val options=layout.getString("options")
        val answers=(1..layout.getInt("question_count")).map { n ->
            val row=answerJson.getJSONObject(n.toString())
            val scores=options.map { fillScore(normalized,coords(row.getJSONArray(it.toString())),2.10) }
            val choice=select(scores)
            if(choice==-2) { warnings.add("Soal $n: tanda ganda atau tidak jelas"); "?" }
            else if(choice==-1) "-" else options[choice].toString()
        }
        val overlay=normalized.copy(Bitmap.Config.ARGB_8888,true)
        val debug=android.graphics.Canvas(overlay)
        val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { style=android.graphics.Paint.Style.STROKE;strokeWidth=1.6f }
        fun ring(xy: Pair<Double,Double>,selected:Boolean){ paint.color=if(selected) Color.rgb(0,145,65) else Color.rgb(115,155,200);debug.drawCircle((xy.first*scale).toFloat(),(xy.second*scale).toFloat(),(2.0*scale).toFloat(),paint) }
        for(d in 1..3){ val row=digits.getJSONObject(d.toString());for(i in 0..9)ring(coords(row.getJSONArray(i.toString())),code[d-1]==('0'+i)) }
        for(n in 1..layout.getInt("question_count")){ val row=answerJson.getJSONObject(n.toString());for(c in options)ring(coords(row.getJSONArray(c.toString())),answers[n-1]==c.toString()) }
        return Result(code.toString(),answers,warnings,normalized,overlay)
    }

    /**
     * Sample the central ink area, excluding the printed outline. Estimate background
     * from a surrounding annulus. Small registration offsets are handled by a local
     * 3x3 center search; use the median of the strongest samples, not one pixel.
     * Score is normalized dark-pixel coverage, not a global mean contrast threshold.
     */
    private fun fillScore(image: Bitmap, xy: Pair<Double,Double>, radiusMm: Double): Double {
        val cx=(xy.first*scale).roundToInt()
        val cy=(xy.second*scale).roundToInt()
        val core=max(3,(radiusMm*scale*0.56).roundToInt())
        val outer=max(core+3,(radiusMm*scale*1.85).roundToInt())
        val ringStart=(radiusMm*scale*1.35).roundToInt()
        var backgroundSum=0.0;var backgroundCount=0
        for (dy in -outer..outer step 2) for (dx in -outer..outer step 2) {
            val d2=dx*dx+dy*dy
            if(d2<ringStart*ringStart || d2>outer*outer)continue
            val x=cx+dx;val y=cy+dy
            if(x in 0 until image.width && y in 0 until image.height){
                backgroundSum+=gray(image.getPixel(x,y));backgroundCount++
            }
        }
        if(backgroundCount<8)return 0.0
        val bg=backgroundSum/backgroundCount
        val threshold=min(180.0,bg-38.0)
        if(threshold<65)return 0.0
        val scores=ArrayList<Double>(9)
        for (oy in -2..2 step 2) for(ox in -2..2 step 2) {
            var dark=0;var count=0;var contrast=0.0
            for(dy in -core..core)for(dx in -core..core){
                if(dx*dx+dy*dy>core*core)continue
                val x=cx+ox+dx;val y=cy+oy+dy
                if(x !in 0 until image.width || y !in 0 until image.height)continue
                val g=gray(image.getPixel(x,y)).toDouble()
                if(g<threshold)dark++
                contrast+=(bg-g).coerceAtLeast(0.0)
                count++
            }
            if(count>0){
                val coverage=dark.toDouble()/count
                val darkness=(contrast/count/180.0).coerceIn(0.0,1.0)
                scores.add(coverage*0.72+darkness*0.28)
            }
        }
        return scores.sortedDescending().take(3).average().takeIf{!it.isNaN()}?:0.0
    }

    /** Never force a choice when the two strongest bubbles look equally filled. */
    private fun select(scores: List<Double>): Int {
        if(scores.isEmpty())return -2
        val ranked=scores.indices.sortedByDescending { scores[it] }
        val best=ranked[0];val top=scores[best]
        val second=if(ranked.size>1)scores[ranked[1]] else 0.0
        if(top<0.14)return -1
        if(top<0.24)return -2
        if(second>=0.24 && (top-second<0.15 || second>=top*0.72))return -2
        return best
    }
    /**
     * Multi-threshold connected-component detection of the four printed square marks.
     * Unlike the previous version, the candidate search is not restricted to the
     * outermost quarter of the photograph: a sheet can have large surrounding margins.
     * A square must be compact, sufficiently filled, and consistent with the other
     * three squares. Results follow the layout's TL, BL, TR, BR order.
     */
    private fun findMarks(image: Bitmap): List<Pair<Double,Double>>? {
        val w=image.width; val h=image.height
        val step=max(1, max(w,h)/1100)
        val cols=(w+step-1)/step; val rows=(h+step-1)/step
        data class Candidate(val x:Double,val y:Double,val size:Double,val fill:Double)
        for(threshold in intArrayOf(75,105,135,165)) {
            val mask=BooleanArray(cols*rows)
            for(y in 0 until rows) for(x in 0 until cols) {
                mask[y*cols+x]=gray(image.getPixel(min(w-1,x*step),min(h-1,y*step)))<threshold
            }
            val seen=BooleanArray(mask.size)
            val queue=IntArray(mask.size)
            val candidates=ArrayList<Candidate>()
            val minSide=max(3,min(cols,rows)/180)
            val maxSide=max(15,min(cols,rows)/8)
            for(i in mask.indices) {
                if(!mask[i]||seen[i])continue
                var head=0;var tail=1;queue[0]=i;seen[i]=true
                var minX=cols;var maxX=0;var minY=rows;var maxY=0
                var sumX=0L;var sumY=0L
                while(head<tail) {
                    val v=queue[head++];val x=v%cols;val y=v/cols
                    sumX+=x;sumY+=y
                    minX=min(minX,x);maxX=max(maxX,x);minY=min(minY,y);maxY=max(maxY,y)
                    if(x>0){val j=v-1;if(mask[j]&&!seen[j]){seen[j]=true;queue[tail++]=j}}
                    if(x+1<cols){val j=v+1;if(mask[j]&&!seen[j]){seen[j]=true;queue[tail++]=j}}
                    if(y>0){val j=v-cols;if(mask[j]&&!seen[j]){seen[j]=true;queue[tail++]=j}}
                    if(y+1<rows){val j=v+cols;if(mask[j]&&!seen[j]){seen[j]=true;queue[tail++]=j}}
                }
                val bw=maxX-minX+1;val bh=maxY-minY+1
                val ratio=bw.toDouble()/bh
                val fill=tail.toDouble()/(bw*bh)
                if(bw in minSide..maxSide && bh in minSide..maxSide && ratio in 0.68..1.46 && fill>0.66) {
                    candidates.add(Candidate(sumX.toDouble()/tail*step,sumY.toDouble()/tail*step,(bw+bh)*step/2.0,fill))
                }
            }
            if(candidates.size<4)continue
            // Search candidates in broad quadrants, then validate the rectangle.
            val quadrants=listOf(
                candidates.filter{it.x<w*.52 && it.y<h*.52},
                candidates.filter{it.x<w*.52 && it.y>h*.48},
                candidates.filter{it.x>w*.48 && it.y<h*.52},
                candidates.filter{it.x>w*.48 && it.y>h*.48}
            )
            if(quadrants.any{it.isEmpty()})continue
            val ranked=quadrants.mapIndexed{index,list->
                list.sortedBy { c ->
                    val tx=if(index==0||index==1) 0.0 else w.toDouble()
                    val ty=if(index==0||index==2) 0.0 else h.toDouble()
                    hypot(c.x-tx,c.y-ty) / hypot(w.toDouble(),h.toDouble()) + (1.0-c.fill)*0.12
                }.take(10)
            }
            var best:List<Candidate>?=null;var bestError=Double.POSITIVE_INFINITY
            for(a in ranked[0])for(b in ranked[1])for(c in ranked[2])for(d in ranked[3]) {
                val widthTop=c.x-a.x;val widthBottom=d.x-b.x
                val heightLeft=b.y-a.y;val heightRight=d.y-c.y
                if(widthTop<w*.32||widthBottom<w*.32||heightLeft<h*.32||heightRight<h*.32)continue
                val sizes=listOf(a.size,b.size,c.size,d.size)
                if(sizes.maxOrNull()!! / sizes.minOrNull()!! > 2.5)continue
                val horizontal=abs(widthTop-widthBottom)/max(widthTop,widthBottom)
                val vertical=abs(heightLeft-heightRight)/max(heightLeft,heightRight)
                val slant=(abs(a.y-c.y)+abs(b.y-d.y))/h+(abs(a.x-b.x)+abs(c.x-d.x))/w
                val error=horizontal+vertical+slant*.35+(sizes.maxOrNull()!!-sizes.minOrNull()!!)/sizes.maxOrNull()!!*.2
                if(error<bestError){bestError=error;best=listOf(a,b,c,d)}
            }
            if(best!=null && bestError<1.0)return best!!.map{it.x to it.y}
        }
        return null
    }
}
