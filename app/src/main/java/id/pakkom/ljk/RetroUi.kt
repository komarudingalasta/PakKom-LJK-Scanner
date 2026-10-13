package id.pakkom.ljk

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/** Reusable, lightweight retro UI: no new dependencies or image assets. */
object RetroUi {
    // Opsi 3: Retro Modern — teal, burnt orange, ivory; clean and uncluttered.
    val paper = Color.rgb(246, 247, 242)
    val cream = Color.rgb(255, 255, 251)
    val green = Color.rgb(0, 91, 88)
    val greenLight = Color.rgb(225, 243, 236)
    val ink = Color.rgb(31, 51, 51)
    val brown = Color.rgb(94, 110, 107)
    val orange = Color.rgb(226, 103, 47)
    val line = Color.rgb(222, 231, 225)

    fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density + .5f).toInt()

    fun background(color: Int, radius: Float = 16f, stroke: Int = line, width: Int = 1): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            if (width > 0) setStroke(width, stroke)
        }

    fun panel(context: Context, color: Int = cream): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        background = background(color, context.dp(22).toFloat(), line, context.dp(1))
        setPadding(context.dp(18), context.dp(18), context.dp(18), context.dp(18))
    }

    fun text(context: Context, value: String, size: Float = 16f, bold: Boolean = false, color: Int = ink): TextView =
        TextView(context).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        }

    fun action(context: Context, label: String, color: Int = green, foreground: Int = cream, click: () -> Unit): TextView =
        text(context, label, 17f, true, foreground).apply {
            gravity = Gravity.CENTER
            minHeight = context.dp(56)
            setPadding(context.dp(10), context.dp(12), context.dp(10), context.dp(12))
            background = background(color, context.dp(16).toFloat(), if (color == cream) line else color, context.dp(1))
            isClickable = true
            isFocusable = true
            setOnClickListener { click() }
        }

    fun add(parent: LinearLayout, child: View, top: Int = 0, bottom: Int = 0) {
        parent.addView(child, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = parent.context.dp(top)
            bottomMargin = parent.context.dp(bottom)
        })
    }
}
