package id.pakkom.ljk

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

/** A guide only; not a claim that corners have been detected. */
class ScanGuideView(context:Context):View(context){
    private val paint=Paint(3).apply {color=Color.rgb(80,230,160);style=Paint.Style.STROKE;strokeWidth=5f}
    override fun onDraw(canvas:Canvas){
        super.onDraw(canvas)
        val left=width*.09f;val right=width*.91f;val top=height*.07f;val bottom=height*.93f
        val n=35f
        for(x in listOf(left,right))for(y in listOf(top,bottom)){
            val dx=if(x==left)n else -n;val dy=if(y==top)n else -n
            canvas.drawLine(x,y,x+dx,y,paint);canvas.drawLine(x,y,x,y+dy,paint)
        }
    }
}
