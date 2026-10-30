package com.cartoonquest.game

import android.graphics.*

/** Konversi literal warna 0xAARRGGBB (Long) ke Int agar tidak error tipe di Kotlin. */
fun K(v: Long): Int = v.toInt()

/** Kumpulan helper gambar berwarna: gradient, outline kartun, teks bergaris, dll. */
class Art {
    var cv: Canvas? = null
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rf = RectF()
    private val boldFace: Typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    private val normalFace: Typeface = Typeface.create("sans-serif", Typeface.NORMAL)

    private fun fill(c: Int) { paint.shader = null; paint.style = Paint.Style.FILL; paint.color = c }

    fun rect(c: Int, x: Float, y: Float, w: Float, h: Float, r: Float = 0f) {
        fill(c); rf.set(x, y, x + w, y + h); cv?.drawRoundRect(rf, r, r, paint)
    }

    fun gradRect(top: Int, bottom: Int, x: Float, y: Float, w: Float, h: Float, r: Float = 0f) {
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(x, y, x, y + h, top, bottom, Shader.TileMode.CLAMP)
        rf.set(x, y, x + w, y + h); cv?.drawRoundRect(rf, r, r, paint)
        paint.shader = null
    }

    fun strokeRect(c: Int, x: Float, y: Float, w: Float, h: Float, r: Float, sw: Float) {
        paint.shader = null; paint.style = Paint.Style.STROKE; paint.strokeWidth = sw; paint.color = c
        rf.set(x, y, x + w, y + h); cv?.drawRoundRect(rf, r, r, paint)
        paint.style = Paint.Style.FILL
    }

    fun circle(c: Int, x: Float, y: Float, r: Float) { fill(c); cv?.drawCircle(x, y, r, paint) }

    fun ring(c: Int, x: Float, y: Float, r: Float, sw: Float) {
        paint.shader = null; paint.style = Paint.Style.STROKE; paint.strokeWidth = sw; paint.color = c
        cv?.drawCircle(x, y, r, paint); paint.style = Paint.Style.FILL
    }

    /** Lingkaran dengan outline gelap ala kartun. */
    fun blob(c: Int, x: Float, y: Float, r: Float, outline: Int = K(0xFF2B1B3D), ow: Float = 3f) {
        circle(outline, x, y, r + ow); circle(c, x, y, r)
    }

    fun radial(inner: Int, outer: Int, x: Float, y: Float, r: Float) {
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(x, y, r, inner, outer, Shader.TileMode.CLAMP)
        cv?.drawCircle(x, y, r, paint); paint.shader = null
    }

    fun oval(c: Int, cx: Float, cy: Float, rx: Float, ry: Float) {
        fill(c); rf.set(cx - rx, cy - ry, cx + rx, cy + ry); cv?.drawOval(rf, paint)
    }

    fun line(c: Int, x1: Float, y1: Float, x2: Float, y2: Float, sw: Float) {
        paint.shader = null; paint.style = Paint.Style.STROKE; paint.strokeWidth = sw
        paint.strokeCap = Paint.Cap.ROUND; paint.color = c
        cv?.drawLine(x1, y1, x2, y2, paint); paint.style = Paint.Style.FILL
    }

    fun arc(c: Int, cx: Float, cy: Float, r: Float, start: Float, sweep: Float, sw: Float) {
        paint.shader = null; paint.style = Paint.Style.STROKE; paint.strokeWidth = sw
        paint.strokeCap = Paint.Cap.ROUND; paint.color = c
        rf.set(cx - r, cy - r, cx + r, cy + r); cv?.drawArc(rf, start, sweep, false, paint)
        paint.style = Paint.Style.FILL
    }

    fun poly(c: Int, vararg pts: Float) {
        fill(c); path.reset(); path.moveTo(pts[0], pts[1])
        var i = 2
        while (i < pts.size - 1) { path.lineTo(pts[i], pts[i + 1]); i += 2 }
        path.close(); cv?.drawPath(path, paint)
    }

    fun hill(c: Int, x: Float, baseY: Float, w: Float, peak: Float) {
        fill(c); path.reset(); path.moveTo(x - w * .1f, baseY)
        path.quadTo(x + w * .5f, baseY - peak * 2f, x + w * 1.1f, baseY)
        path.close(); cv?.drawPath(path, paint)
    }

    fun star(c: Int, cx: Float, cy: Float, r: Float, rot: Float = 0f) {
        fill(c); path.reset()
        for (i in 0 until 10) {
            val rr = if (i % 2 == 0) r else r * .45f
            val a = rot + i * Math.PI.toFloat() / 5f - Math.PI.toFloat() / 2f
            val px = cx + kotlin.math.cos(a) * rr; val py = cy + kotlin.math.sin(a) * rr
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close(); cv?.drawPath(path, paint)
    }

    fun heart(c: Int, cx: Float, cy: Float, s: Float) {
        fill(c); path.reset()
        path.moveTo(cx, cy + s)
        path.cubicTo(cx - s * 1.6f, cy - s * .1f, cx - s * .7f, cy - s * 1.2f, cx, cy - s * .4f)
        path.cubicTo(cx + s * .7f, cy - s * 1.2f, cx + s * 1.6f, cy - s * .1f, cx, cy + s)
        path.close(); cv?.drawPath(path, paint)
    }

    fun text(s: String, x: Float, y: Float, size: Float, c: Int = Color.WHITE, bold: Boolean = true,
             shadow: Int = 0, center: Boolean = false) {
        paint.shader = null; paint.style = Paint.Style.FILL; paint.textSize = size
        paint.typeface = if (bold) boldFace else normalFace
        paint.textAlign = if (center) Paint.Align.CENTER else Paint.Align.LEFT
        if (shadow != 0) { paint.color = shadow; cv?.drawText(s, x + size * .06f, y + size * .08f, paint) }
        paint.color = c; cv?.drawText(s, x, y, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    /** Teks dengan garis tepi tebal (gaya judul game kartun). */
    fun titleText(s: String, x: Float, y: Float, size: Float, fillC: Int, strokeC: Int, center: Boolean = false) {
        paint.shader = null; paint.textSize = size; paint.typeface = boldFace
        paint.textAlign = if (center) Paint.Align.CENTER else Paint.Align.LEFT
        paint.style = Paint.Style.STROKE; paint.strokeWidth = size * .16f; paint.strokeJoin = Paint.Join.ROUND
        paint.color = strokeC; cv?.drawText(s, x, y + size * .05f, paint)
        paint.style = Paint.Style.FILL; paint.color = fillC; cv?.drawText(s, x, y, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    fun textWidth(s: String, size: Float): Float { paint.textSize = size; paint.typeface = boldFace; return paint.measureText(s) }

    /** Tombol berwarna: bayangan, gradient, kilap, outline. */
    fun button(label: String, x: Float, y: Float, w: Float, h: Float, top: Int, bottom: Int) {
        rect(K(0x66000000), x + 3f, y + 7f, w, h, h * .35f)
        gradRect(top, bottom, x, y, w, h, h * .35f)
        rect(K(0x55FFFFFF), x + w * .06f, y + h * .08f, w * .88f, h * .30f, h * .2f)
        strokeRect(K(0xFF2B1B3D), x, y, w, h, h * .35f, 3.5f)
        titleText(label, x + w / 2f, y + h * .66f, h * .40f, Color.WHITE, K(0xFF2B1B3D), true)
    }
}
