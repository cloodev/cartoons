package com.cartoonquest.game

import android.graphics.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Semua gambar dunia: langit, bukit, awan, dekorasi tema, karakter, musuh, koin. */
class Scenes(private val a: Art) {
    private val OUT = K(0xFF2B1B3D)

    fun background(wd: World, w: Float, h: Float, cam: Float, time: Float) {
        a.gradRect(wd.skyTop, wd.skyBottom, 0f, 0f, w, h)
        // matahari / bulan
        val night = wd.theme == 7 || wd.theme == 9 || wd.theme == 11
        val sx = w * .82f; val sy = h * .2f
        if (night) {
            a.radial(K(0x66FFFFFF), K(0x00FFFFFF), sx, sy, h * .22f)
            a.circle(K(0xFFFFF8D6), sx, sy, h * .075f)
            a.circle(wd.skyTop, sx + h * .03f, sy - h * .015f, h * .065f)
            for (i in 0 until 26) {
                val tx = (i * 97.3f) % w; val ty = (i * 53.7f) % (h * .6f)
                val tw = 0.5f + 0.5f * sin(time * 2f + i)
                a.star(K(0xFFFFFFFF), tx, ty, 2f + 3f * tw, time * .2f)
            }
        } else {
            a.radial(K(0x99FFF3B0), K(0x00FFF3B0), sx, sy, h * .3f)
            a.circle(K(0xFFFFF176), sx, sy, h * .09f)
            a.circle(K(0xFFFFEE58), sx, sy, h * .07f)
        }
        // bukit jauh & dekat (parallax)
        val gy = h * .78f
        for (i in -1..4) {
            val x = wrap(i * w * .42f - cam * .12f, w * 1.7f) - w * .3f
            a.hill(wd.hillFar, x, gy, w * .55f, h * .26f)
        }
        for (i in -1..4) {
            val x = wrap(i * w * .36f - cam * .25f, w * 1.5f) - w * .3f
            a.hill(wd.hillNear, x + w * .1f, gy, w * .42f, h * .17f)
        }
        // awan
        for (i in 0..5) {
            val span = w + 320f
            val x = (((i * 260f - cam * .08f + time * 8f * (1 + i % 3)) % span) + span) % span - 160f
            val y = h * (.1f + (i % 3) * .09f) + sin(time * .8f + i) * 5f
            cloud(x, y, 1f + (i % 2) * .35f)
        }
        decor(wd, w, h, cam, time)
    }

    private fun cloud(x: Float, y: Float, s: Float) {
        val c = K(0xCCFFFFFF)
        a.circle(c, x, y, 22f * s); a.circle(c, x + 26f * s, y - 12f * s, 29f * s)
        a.circle(c, x + 56f * s, y - 2f * s, 24f * s); a.rect(c, x - 8f * s, y, 78f * s, 20f * s, 10f * s)
    }

    private fun decor(wd: World, w: Float, h: Float, cam: Float, time: Float) {
        when (wd.theme) {
            3 -> for (i in 0 until 40) { // salju
                val x = (i * 71.3f + sin(time + i) * 20f - cam * .1f) % w
                val y = (i * 37.9f + time * (30f + i % 5 * 12f)) % h
                a.circle(K(0xDDFFFFFF), (x + w) % w, y, 2.5f + i % 3)
            }
            4, 10 -> for (i in 0 until 26) { // bara api
                val x = (i * 83.1f + sin(time * 1.5f + i) * 15f) % w
                val y = h - ((i * 41.7f + time * (40f + i % 4 * 20f)) % h)
                a.circle(K(0xCCFFB300), x, y, 2f + i % 3); a.circle(K(0x66FF5722), x, y, 5f + i % 3)
            }
            8 -> for (i in 0 until 22) { // gelembung
                val x = (i * 91.7f + sin(time + i) * 14f) % w
                val y = h - ((i * 47.3f + time * (25f + i % 4 * 10f)) % h)
                a.ring(K(0xAAFFFFFF), x, y, 5f + i % 4 * 2f, 2f)
            }
            5 -> for (i in 0 until 14) { // permen melayang
                val x = (i * 131f - cam * .15f) % w
                val y = h * .15f + (i * 57f) % (h * .4f) + sin(time + i) * 8f
                val cc = intArrayOf(K(0xFFFF4081), K(0xFF40C4FF), K(0xFFFFEB3B), K(0xFF69F0AE))[i % 4]
                a.circle(cc, (x + w) % w, y, 7f)
            }
            1 -> for (i in 0 until 5) { // jamur raksasa di latar
                val x = ((i * w * .3f - cam * .3f) % (w * 1.5f) + w * 1.5f) % (w * 1.5f) - 60f
                val gy = h * .78f
                a.rect(K(0xFFFFF3E0), x - 10f, gy - 70f, 20f, 70f, 8f)
                a.oval(if (i % 2 == 0) K(0xFFFF5252) else K(0xFFBA68C8), x, gy - 72f, 48f, 32f)
                a.circle(K(0xFFFFFFFF), x - 16f, gy - 80f, 6f); a.circle(K(0xFFFFFFFF), x + 12f, gy - 70f, 5f)
            }
            0 -> for (i in 0 until 6) { // pohon di latar
                val x = ((i * w * .27f - cam * .32f) % (w * 1.6f) + w * 1.6f) % (w * 1.6f) - 60f
                val gy = h * .78f
                a.rect(K(0xFF8D5A2B), x - 7f, gy - 60f, 14f, 60f, 5f)
                a.circle(K(0xFF2E9E4F), x, gy - 78f, 34f); a.circle(K(0xFF49C26A), x - 14f, gy - 90f, 22f)
            }
            2 -> for (i in 0 until 5) { // kaktus di latar
                val x = ((i * w * .31f - cam * .3f) % (w * 1.6f) + w * 1.6f) % (w * 1.6f) - 40f
                val gy = h * .78f
                a.rect(K(0xFF2E9E4F), x - 9f, gy - 80f, 18f, 80f, 9f)
                a.rect(K(0xFF2E9E4F), x - 28f, gy - 55f, 12f, 28f, 6f); a.rect(K(0xFF2E9E4F), x + 16f, gy - 62f, 12f, 28f, 6f)
            }
            7, 11 -> for (i in 0 until 4) { // menara kastil
                val x = ((i * w * .38f - cam * .28f) % (w * 1.6f) + w * 1.6f) % (w * 1.6f) - 40f
                val gy = h * .78f
                a.rect(K(0xFF3A2F66), x - 24f, gy - 150f, 48f, 150f, 4f)
                a.poly(K(0xFF5B3FA0), x - 30f, gy - 150f, x, gy - 205f, x + 30f, gy - 150f)
                a.rect(K(0xFFFFEB3B), x - 5f, gy - 110f, 10f, 18f, 3f)
            }
            9 -> for (i in 0 until 7) { // kristal
                val x = ((i * w * .22f - cam * .3f) % (w * 1.6f) + w * 1.6f) % (w * 1.6f) - 30f
                val gy = h * .78f
                a.poly(K(0xFF64FFDA), x - 16f, gy, x, gy - 75f - (i % 3) * 20f, x + 16f, gy)
                a.poly(K(0x88FFFFFF), x - 5f, gy, x, gy - 70f - (i % 3) * 20f, x + 2f, gy)
            }
            6 -> for (i in 0 until 3) { // pulau melayang
                val x = ((i * w * .5f - cam * .2f) % (w * 1.6f) + w * 1.6f) % (w * 1.6f) - 60f
                val y = h * .45f + sin(time + i) * 8f + i * 12f
                a.oval(K(0xFF9BE86B), x, y, 70f, 16f); a.poly(K(0xFFB58B5A), x - 60f, y + 4f, x, y + 60f, x + 60f, y + 4f)
            }
        }
    }

    fun platform(wd: World, p: Platform) {
        // tanah dengan gradient + rumput bergelombang + highlight
        a.rect(K(0x44000000), p.x + 3f, p.y + 5f, p.w, p.h + 8f, 9f)
        a.gradRect(wd.dirt, darker(wd.dirt), p.x, p.y, p.w, p.h + 6f, 9f)
        a.rect(K(0x22FFFFFF), p.x + 6f, p.y + 12f, p.w - 12f, 3f, 2f)
        a.rect(wd.grass, p.x - 3f, p.y - 5f, p.w + 6f, 14f, 8f)
        a.rect(K(0x55FFFFFF), p.x + 4f, p.y - 3f, p.w - 8f, 4f, 3f)
        var i = 0
        while (i < p.w / 26f) {
            a.circle(darker(wd.grass), p.x + 10f + i * 26f, p.y + 9f, 5f); i++
        }
        a.strokeRect(OUT, p.x - 3f, p.y - 5f, p.w + 6f, p.h + 11f, 9f, 2.5f)
    }

    fun spike(z: Hazard, time: Float) {
        val n = (z.w / 17f).toInt().coerceAtLeast(2); val sw = z.w / n
        for (i in 0 until n) {
            val x = z.x + i * sw
            a.poly(OUT, x - 2f, z.y + 27f, x + sw / 2f, z.y - 15f, x + sw + 2f, z.y + 27f)
            a.poly(K(0xFFFF5252), x, z.y + 25f, x + sw / 2f, z.y - 11f, x + sw, z.y + 25f)
            a.poly(K(0xFFFFB4B4), x + sw * .35f, z.y + 22f, x + sw / 2f, z.y - 5f, x + sw * .55f, z.y + 22f)
        }
    }

    fun coin(cx: Float, cy: Float, time: Float, phase: Float) {
        val squash = abs(cos(time * 3f + phase)).coerceAtLeast(.25f)
        val bob = sin(time * 4f + phase) * 3f
        a.radial(K(0x88FFEB3B), K(0x00FFEB3B), cx, cy + bob, 22f)
        a.oval(OUT, cx, cy + bob, 13f * squash + 2.5f, 15f)
        a.oval(K(0xFFFFB300), cx, cy + bob, 13f * squash, 12.5f)
        a.oval(K(0xFFFFE066), cx - 1f, cy + bob - 1f, 9f * squash, 9f)
        a.rect(K(0xFFFFB300), cx - 2f * squash, cy + bob - 5f, 4f * squash, 10f, 2f)
    }

    fun enemy(wd: World, e: Enemy, time: Float) {
        val bob = sin(time * 8f + e.x * .05f) * 3f
        val x = e.x; val y = e.y + bob
        val body = if (e.flying) K(0xFFB388FF) else wd.enemyColor
        if (e.flying) { // sayap
            val flap = sin(time * 20f) * 10f
            a.oval(K(0xCCFFFFFF), x - 24f, y - 8f - flap, 14f, 8f); a.oval(K(0xCCFFFFFF), x + 24f, y - 8f - flap, 14f, 8f)
        } else { // kaki
            a.oval(OUT, x - 9f, y + 19f, 8f, 5f); a.oval(OUT, x + 9f, y + 19f, 8f, 5f)
        }
        a.blob(body, x, y, 20f, OUT, 3f)
        a.oval(K(0x55FFFFFF), x - 7f, y - 11f, 8f, 4f)
        // mata besar
        a.circle(Color.WHITE, x - 7f, y - 3f, 6.5f); a.circle(Color.WHITE, x + 7f, y - 3f, 6.5f)
        val look = e.dir * 2f
        a.circle(OUT, x - 7f + look, y - 2f, 3f); a.circle(OUT, x + 7f + look, y - 2f, 3f)
        // alis marah + mulut bertaring
        a.line(OUT, x - 13f, y - 12f, x - 3f, y - 8f, 3f); a.line(OUT, x + 13f, y - 12f, x + 3f, y - 8f, 3f)
        a.rect(OUT, x - 9f, y + 7f, 18f, 7f, 4f)
        a.poly(Color.WHITE, x - 6f, y + 7f, x - 3f, y + 12f, x, y + 7f)
        a.poly(Color.WHITE, x + 1f, y + 7f, x + 4f, y + 12f, x + 7f, y + 7f)
        if (wd.theme == 0 || wd.theme == 1) { // tanduk kecil
            a.poly(OUT, x - 12f, y - 17f, x - 8f, y - 28f, x - 3f, y - 19f)
            a.poly(OUT, x + 12f, y - 17f, x + 8f, y - 28f, x + 3f, y - 19f)
        }
    }

    fun boss(wd: World, x: Float, gy: Float, flash: Boolean, hp: Int, time: Float) {
        val y = gy - 58f + sin(time * 4f) * 3f
        val body = if (flash) Color.WHITE else K(0xFFB5446E)
        a.oval(K(0x44000000), x, gy + 2f, 48f, 8f)
        // tanduk, perut, tubuh
        a.poly(OUT, x - 36f, y - 20f, x - 28f, y - 62f, x - 10f, y - 38f)
        a.poly(OUT, x + 36f, y - 20f, x + 28f, y - 62f, x + 10f, y - 38f)
        a.poly(K(0xFFFFD54F), x - 32f, y - 24f, x - 27f, y - 54f, x - 14f, y - 38f)
        a.poly(K(0xFFFFD54F), x + 32f, y - 24f, x + 27f, y - 54f, x + 14f, y - 38f)
        a.blob(body, x, y, 44f, OUT, 4f)
        a.oval(if (flash) Color.WHITE else K(0xFFFFB6C9), x, y + 18f, 26f, 18f)
        a.oval(K(0x55FFFFFF), x - 16f, y - 26f, 14f, 7f)
        // mata menyala
        a.circle(Color.WHITE, x - 16f, y - 8f, 11f); a.circle(Color.WHITE, x + 16f, y - 8f, 11f)
        a.circle(K(0xFFFF1744), x - 14f, y - 7f, 5.5f); a.circle(K(0xFFFF1744), x + 14f, y - 7f, 5.5f)
        a.line(OUT, x - 28f, y - 24f, x - 6f, y - 15f, 5f); a.line(OUT, x + 28f, y - 24f, x + 6f, y - 15f, 5f)
        // mulut bergigi
        a.rect(OUT, x - 22f, y + 12f, 44f, 15f, 7f)
        for (i in 0 until 4) a.poly(Color.WHITE, x - 20f + i * 11f, y + 12f, x - 14.5f + i * 11f, y + 21f, x - 9f + i * 11f, y + 12f)
        // bar nyawa
        a.rect(OUT, x - 42f, y - 86f, 84f, 14f, 7f)
        a.rect(K(0xFF8A1F2D), x - 39f, y - 83f, 78f, 8f, 4f)
        if (hp > 0) a.gradRect(K(0xFF9CFF7A), K(0xFF2EBF4F), x - 39f, y - 83f, 78f * (hp / 4f), 8f, 4f)
    }

    fun flag(wd: World, gx: Float, gy: Float, time: Float) {
        a.oval(K(0x44000000), gx + 3f, gy + 2f, 20f, 5f)
        a.rect(OUT, gx - 2f, gy - 136f, 11f, 136f, 5f)
        a.gradRect(K(0xFFFFFFFF), K(0xFFCFD8DC), gx, gy - 134f, 7f, 134f, 3f)
        a.circle(OUT, gx + 3.5f, gy - 138f, 10f); a.circle(K(0xFFFFD54F), gx + 3.5f, gy - 138f, 7f)
        val wave = sin(time * 6f) * 6f
        a.poly(OUT, gx + 7f, gy - 130f, gx + 80f, gy - 106f + wave, gx + 7f, gy - 80f)
        a.poly(wd.accent, gx + 9f, gy - 125f, gx + 70f, gy - 106f + wave, gx + 9f, gy - 86f)
        a.star(Color.WHITE, gx + 30f, gy - 106f + wave * .4f, 9f, time)
    }

    fun hero(x: Float, y: Float, speed: Float, vy: Float, facing: Float, time: Float, blink: Boolean) {
        val moving = abs(speed) > 20f
        val bob = if (moving) abs(sin(time * 14f)) * 3f else sin(time * 2f) * 1.5f
        val yy = y + bob
        val cx = x + 20f
        val swing = if (moving) sin(time * 14f) * 6f else 0f
        val air = vy != 0f && abs(vy) > 40f
        a.oval(K(0x44000000), cx, y + 50f, 19f, 5f)
        // kaki / sepatu
        val lf = if (air) -3f else swing; val rf = if (air) 3f else -swing
        a.oval(OUT, cx - 9f + lf, yy + 43f, 10f, 7f); a.oval(K(0xFFFF7043), cx - 9f + lf, yy + 42f, 8f, 5.5f)
        a.oval(OUT, cx + 9f + rf, yy + 43f, 10f, 7f); a.oval(K(0xFFFF7043), cx + 9f + rf, yy + 42f, 8f, 5.5f)
        // tubuh
        a.rect(OUT, cx - 16f, yy + 18f, 32f, 26f, 11f)
        a.gradRect(K(0xFF5AB0FF), K(0xFF2A6FD8), cx - 14f, yy + 20f, 28f, 22f, 9f)
        a.rect(K(0xFFFFD54F), cx - 14f, yy + 33f, 28f, 4f, 2f)
        a.circle(K(0xFFFFD54F), cx, yy + 35f, 3.5f)
        // lengan
        val arm = if (air) -8f else swing * .7f
        a.circle(OUT, cx - 17f * facing, yy + 28f + arm, 6f); a.circle(K(0xFFFFC9A0), cx - 17f * facing, yy + 28f + arm, 4f)
        a.circle(OUT, cx + 17f * facing, yy + 28f - arm, 6f); a.circle(K(0xFFFFC9A0), cx + 17f * facing, yy + 28f - arm, 4f)
        // kepala
        a.blob(K(0xFFFFC9A0), cx, yy + 11f, 17f, OUT, 3f)
        a.circle(K(0x66FF8A80), cx - 10f, yy + 17f, 4f); a.circle(K(0x66FF8A80), cx + 10f, yy + 17f, 4f)
        // topi merah dengan bintang
        a.rect(OUT, cx - 21f * 1f, yy - 3f, 42f, 12f, 6f)
        a.rect(K(0xFFFF5252), cx - 19f, yy - 2f, 38f, 9f, 5f)
        a.circle(OUT, cx, yy - 6f, 17f)
        a.circle(K(0xFFFF5252), cx, yy - 6f, 14f)
        a.rect(K(0xFFFF5252), cx - 14f, yy - 6f, 28f, 8f, 0f)
        a.oval(K(0x66FFFFFF), cx - 5f, yy - 12f, 6f, 3f)
        a.star(K(0xFFFFEB3B), cx + 2f * facing, yy - 5f, 5f)
        // mata
        val ex = 3f * facing
        if (blink) {
            a.line(OUT, cx - 10f + ex, yy + 12f, cx - 4f + ex, yy + 12f, 2.5f); a.line(OUT, cx + 4f + ex, yy + 12f, cx + 10f + ex, yy + 12f, 2.5f)
        } else {
            a.circle(Color.WHITE, cx - 7f + ex, yy + 12f, 5.5f); a.circle(Color.WHITE, cx + 7f + ex, yy + 12f, 5.5f)
            a.circle(OUT, cx - 6f + ex + facing * 1.5f, yy + 12f, 3f); a.circle(OUT, cx + 8f + ex + facing * 1.5f, yy + 12f, 3f)
            a.circle(Color.WHITE, cx - 5f + ex + facing * 1.5f, yy + 10.5f, 1f); a.circle(Color.WHITE, cx + 9f + ex + facing * 1.5f, yy + 10.5f, 1f)
        }
        // senyum
        a.arc(OUT, cx + ex, yy + 16f, 5f, 20f, 140f, 2f)
    }

    private fun wrap(v: Float, m: Float): Float = ((v % m) + m) % m

    fun darker(c: Int): Int {
        val r = (Color.red(c) * .72f).toInt(); val g = (Color.green(c) * .72f).toInt(); val b = (Color.blue(c) * .72f).toInt()
        return Color.rgb(r, g, b)
    }
}
