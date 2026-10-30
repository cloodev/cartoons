package com.cartoonquest.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

class GameView(ctx: Context) : View(ctx) {
    private val prefs = ctx.getSharedPreferences("cartoonquest", Context.MODE_PRIVATE)
    private val art = Art()
    private val scenes = Scenes(art)
    private val OUT = K(0xFF2B1B3D)
    private val worlds = Worlds.all

    private var screen = 0 // 0 lobby, 1 world select, 2 level select, 3 game, 4 win
    private var world = 0
    private var level = 1
    private var unlocked = prefs.getInt("unlocked", 1).coerceIn(1, 241)
    private var bestCoins = prefs.getInt("coins", 0)
    private var coinsNow = 0
    private var playerX = 90f; private var playerY = 0f; private var vx = 0f; private var vy = 0f
    private var onGround = false; private var facing = 1f; private var camera = 0f
    private var left = false; private var right = false; private var jumpQueued = false
    private var time = 0f; private var hp = 3; private var invul = 0f
    private var checkX = 80f; private var checkY = 0f
    private var bossX = -1f; private var bossHp = 0; private var bossFlash = 0f
    private val particles = ArrayList<Particle>()
    private var tone: ToneGenerator? = null
    private var music: MediaPlayer? = null
    private var paused = false
    private var inputLock = 0f
    private var leftPointer = -1; private var rightPointer = -1; private var jumpPointer = -1
    private val platforms = ArrayList<Platform>(); private val enemies = ArrayList<Enemy>()
    private val coins = ArrayList<Coin>(); private val hazards = ArrayList<Hazard>()
    private var goalX = 0f; private var worldWidth = 2200f
    private var last = System.nanoTime()
    private val w get() = width.toFloat()
    private val h get() = height.toFloat()
    private val groundY get() = h * .78f
    private val unlockedWorld get() = ((unlocked - 1) / 20).coerceIn(0, 11)
    private val unlockedLevel get() = ((unlocked - 1) % 20 + 1).coerceIn(1, 20)

    init {
        isFocusable = true
        try { tone = ToneGenerator(AudioManager.STREAM_MUSIC, 55) } catch (_: Exception) { tone = null }
        try {
            val mp = MediaPlayer()
            val afd = context.assets.openFd("music_loop.wav")
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.isLooping = true
            mp.setVolume(.20f, .20f)
            mp.prepare()
            mp.start()
            music = mp
        } catch (_: Exception) { music = null }
    }

    fun onAppPause() {
        if (screen == 3) paused = true
        left = false; right = false; leftPointer = -1; rightPointer = -1; jumpPointer = -1
        try { music?.pause() } catch (_: Exception) {}
    }

    fun onAppResume() {
        last = System.nanoTime()
        try { music?.start() } catch (_: Exception) {}
    }

    override fun onDetachedFromWindow() {
        try { music?.stop(); music?.release() } catch (_: Exception) {}
        music = null
        try { tone?.release() } catch (_: Exception) {}
        tone = null
        super.onDetachedFromWindow()
    }

    private fun sfx(kind: Int, duration: Int = 90) {
        val t = when (kind) { 0 -> ToneGenerator.TONE_PROP_BEEP; 1 -> ToneGenerator.TONE_PROP_ACK; 2 -> ToneGenerator.TONE_PROP_PROMPT; else -> ToneGenerator.TONE_PROP_NACK }
        try { tone?.startTone(t, duration) } catch (_: Exception) {}
    }

    private fun burst(x: Float, y: Float, count: Int, c: Int) {
        repeat(count) {
            val a = Random.nextFloat() * (Math.PI * 2).toFloat()
            val sp = 65f + Random.nextFloat() * 180f
            particles.add(Particle(x, y, cos(a) * sp, sin(a) * sp - 55f, .55f, c))
        }
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        art.cv = c
        val now = System.nanoTime()
        val dt = ((now - last) / 1e9).toFloat().coerceIn(0f, .04f); last = now
        time += dt; inputLock = (inputLock - dt).coerceAtLeast(0f)
        when (screen) {
            0 -> drawLobby()
            1 -> drawWorlds()
            2 -> drawLevels()
            3 -> { if (!paused) update(dt); drawGame(); if (paused) drawPause() }
            4 -> drawWin()
        }
        art.cv = null
        postInvalidateOnAnimation()
    }

    private fun bg(i: Int, cam: Float = 0f) = scenes.background(worlds[i], w, h, cam, time)

    // ---------- UI helpers ----------
    private fun coinIcon(x: Float, y: Float, r: Float) {
        art.circle(OUT, x, y, r + 2.5f); art.circle(K(0xFFFFB300), x, y, r)
        art.circle(K(0xFFFFE066), x - r * .15f, y - r * .15f, r * .65f)
        art.rect(K(0xFFFFB300), x - r * .12f, y - r * .4f, r * .24f, r * .8f, 2f)
    }

    private fun panel(x: Float, y: Float, pw: Float, ph: Float) {
        art.rect(K(0x55000000), x + 3f, y + 6f, pw, ph, 20f)
        art.gradRect(K(0xEE3A5FA8), K(0xEE1F2F66), x, y, pw, ph, 20f)
        art.strokeRect(OUT, x, y, pw, ph, 20f, 3.5f)
    }

    private fun drawLobby() {
        bg(0)
        // logo
        val bounce = sin(time * 2f) * h * .008f
        art.titleText("CARTOON", w * .09f, h * .24f + bounce, h * .13f, K(0xFFFFFFFF), OUT)
        art.titleText("QUEST", w * .09f, h * .40f - bounce, h * .15f, K(0xFFFF9D2E), OUT)
        art.star(K(0xFFFFEB3B), w * .47f, h * .20f, h * .045f, time)
        art.star(K(0xFFFF80AB), w * .50f, h * .33f, h * .03f, -time)
        art.text("A LITTLE HERO, A BIG ADVENTURE!", w * .095f, h * .475f, h * .036f, K(0xFF1F3A66), false)
        // ground & hero
        art.rect(K(0xFF5BD35F), 0f, h * .88f, w, h * .12f)
        art.rect(K(0xFF9A6B3F), 0f, h * .93f, w, h * .07f)
        art.rect(K(0xFF3FAE45), 0f, h * .88f, w, h * .02f)
        val hx = w * .68f
        val cvs = art.cv
        cvs?.save(); cvs?.scale(2.4f, 2.4f, hx + 20f, h * .88f)
        scenes.hero(hx, h * .88f - 52f - abs(sin(time * 3f)) * 12f, 0f, 0f, 1f, time, sin(time * .9f) > .97f)
        cvs?.restore()
        art.button("PLAY", w * .1f, h * .56f, w * .27f, h * .14f, K(0xFFFFC04A), K(0xFFFF7A1A))
        art.button("WORLDS", w * .1f, h * .74f, w * .27f, h * .11f, K(0xFF6EC6FF), K(0xFF2A7BE0))
        panel(w * .74f, h * .05f, w * .22f, h * .1f)
        coinIcon(w * .775f, h * .1f, h * .03f)
        art.titleText("$bestCoins", w * .8f, h * .12f, h * .05f, Color.WHITE, OUT)
        art.text("12 WORLDS  •  240 LEVELS", w * .095f, h * .96f, h * .032f, Color.WHITE, true, K(0x88000000))
    }

    private fun worldRect(i: Int): FloatArray {
        val bw = w * .27f; val bh = h * .2f
        return floatArrayOf(w * .06f + (i % 3) * (bw + w * .03f), h * .22f + (i / 3) * (bh + h * .035f), bw, bh)
    }

    private fun drawWorlds() {
        bg(0)
        art.titleText("CHOOSE YOUR WORLD", w * .22f, h * .15f, h * .075f, Color.WHITE, OUT)
        for (i in 0 until 12) {
            val r = worldRect(i); val wd = worlds[i]; val locked = i > unlockedWorld
            art.rect(K(0x55000000), r[0] + 3f, r[1] + 6f, r[2], r[3], 20f)
            if (locked) art.gradRect(K(0xFFB0BEC5), K(0xFF78909C), r[0], r[1], r[2], r[3], 20f)
            else {
                art.gradRect(wd.skyTop, wd.skyBottom, r[0], r[1], r[2], r[3] * .72f, 20f)
                art.gradRect(wd.grass, wd.dirt, r[0], r[1] + r[3] * .62f, r[2], r[3] * .38f, 20f)
                art.circle(K(0xFFFFF176), r[0] + r[2] * .84f, r[1] + r[3] * .22f, r[3] * .1f)
                art.hill(wd.hillNear, r[0] + r[2] * .1f, r[1] + r[3] * .64f, r[2] * .6f, r[3] * .12f)
            }
            art.strokeRect(OUT, r[0], r[1], r[2], r[3], 20f, 3.5f)
            if (locked) {
                art.rect(OUT, r[0] + r[2] * .12f, r[1] + r[3] * .30f, r[3] * .30f, r[3] * .26f, 5f)
                art.arc(OUT, r[0] + r[2] * .12f + r[3] * .15f, r[1] + r[3] * .30f, r[3] * .12f, 180f, 180f, 5f)
                art.circle(K(0xFFFFC107), r[0] + r[2] * .12f + r[3] * .15f, r[1] + r[3] * .43f, r[3] * .04f)
            } else {
                art.star(wd.accent, r[0] + r[2] * .16f, r[1] + r[3] * .30f, r[3] * .17f, sin(time + i) * .2f)
            }
            art.titleText(wd.name, r[0] + r[2] * .06f, r[1] + r[3] * .84f, r[3] * .17f, Color.WHITE, OUT)
            art.text(if (locked) "LOCKED" else "20 LEVELS", r[0] + r[2] * .40f, r[1] + r[3] * .40f, r[3] * .15f, Color.WHITE, true, K(0x88000000))
        }
        art.button("BACK", w * .03f, h * .04f, w * .15f, h * .1f, K(0xFFFF8A80), K(0xFFE53935))
    }

    private fun levelRect(i: Int): FloatArray {
        val col = (i - 1) % 5; val row = (i - 1) / 5
        return floatArrayOf(w * .08f + col * w * .17f, h * .31f + row * h * .15f, w * .13f, h * .115f)
    }

    private fun drawLevels() {
        bg(world)
        art.titleText(worlds[world].name.uppercase(), w * .2f, h * .16f, h * .075f, Color.WHITE, OUT)
        art.text("COMPLETE ALL 20 LEVELS TO UNLOCK THE NEXT WORLD", w * .2f, h * .23f, h * .03f, K(0xFF1F3A66), false)
        for (i in 1..20) {
            val r = levelRect(i); val global = world * 20 + i; val locked = global > unlocked
            val done = global < unlocked
            art.rect(K(0x55000000), r[0] + 3f, r[1] + 6f, r[2], r[3], 16f)
            if (locked) art.gradRect(K(0xFFB0BEC5), K(0xFF78909C), r[0], r[1], r[2], r[3], 16f)
            else if (done) art.gradRect(K(0xFF9CFF8A), K(0xFF2EBF4F), r[0], r[1], r[2], r[3], 16f)
            else art.gradRect(K(0xFFFFD36E), K(0xFFFF8F1F), r[0], r[1], r[2], r[3], 16f)
            art.rect(K(0x55FFFFFF), r[0] + r[2] * .08f, r[1] + r[3] * .07f, r[2] * .84f, r[3] * .28f, 10f)
            art.strokeRect(OUT, r[0], r[1], r[2], r[3], 16f, 3.5f)
            if (locked) {
                val cx = r[0] + r[2] / 2f; val cy = r[1] + r[3] * .58f
                art.arc(OUT, cx, cy - r[3] * .12f, r[3] * .14f, 180f, 180f, 4f)
                art.rect(OUT, cx - r[3] * .2f, cy - r[3] * .1f, r[3] * .4f, r[3] * .3f, 5f)
            } else {
                art.titleText("$i", r[0] + r[2] / 2f, r[1] + r[3] * .72f, h * .055f, Color.WHITE, OUT, true)
                if (i % 5 == 0) art.star(K(0xFFFF5252), r[0] + r[2] * .88f, r[1] + r[3] * .15f, r[3] * .14f, time)
            }
        }
        art.button("BACK", w * .03f, h * .04f, w * .15f, h * .1f, K(0xFFFF8A80), K(0xFFE53935))
    }

    // ---------- level generation ----------
    private fun makeLevel() {
        platforms.clear(); enemies.clear(); coins.clear(); hazards.clear(); particles.clear()
        val boss = level % 5 == 0
        worldWidth = 1900f + level * 45f
        platforms.add(Platform(0f, groundY, w * .7f, 30f))
        var x = max(260f, w * .7f + 40f)
        val rnd = Random(world * 1000 + level * 91)
        val limit = if (boss) worldWidth - 700f else worldWidth - 260f
        while (x < limit) {
            val gap = if (level % 4 == 0) rnd.nextInt(80, 145).toFloat() else rnd.nextInt(30, 85).toFloat()
            val pw = rnd.nextInt(115, 230).toFloat()
            val py = groundY - rnd.nextInt(65, 190)
            platforms.add(Platform(x, py, pw, 22f))
            coins.add(Coin(x + pw * .5f, py - 45f)); coins.add(Coin(x + pw * .5f + 28f, py - 45f))
            if (rnd.nextFloat() < .72f) enemies.add(Enemy(x + 35f, py - 30f, x + 8f, x + pw - 30f, 1f, level % 3 == 0))
            if (rnd.nextFloat() < .48f) hazards.add(Hazard(x + pw + gap * .15f, groundY - 25f, 35f))
            x += pw + gap
            if (x < limit + 40f) platforms.add(Platform(x - gap, groundY, gap + 20f, 30f))
        }
        if (boss) platforms.add(Platform(worldWidth - 640f, groundY, 700f, 30f))
        else platforms.add(Platform(worldWidth - 260f, groundY, 340f, 30f))
        goalX = worldWidth - 150f
        bossX = if (boss) worldWidth - 390f else -1f
        bossHp = if (boss) 4 else 0
        bossFlash = 0f
        for (i in 0..5) coins.add(Coin(450f + i * 230f, groundY - 85f))
        if (boss) enemies.add(Enemy(worldWidth - 430f, groundY - 55f, worldWidth - 550f, worldWidth - 240f, 1.2f, true))
        playerX = 80f; playerY = groundY - 52f; vx = 0f; vy = 0f; camera = 0f; hp = 3; coinsNow = 0
        checkX = 80f; checkY = groundY - 52f; invul = 0f
        left = false; right = false; jumpQueued = false
        leftPointer = -1; rightPointer = -1; jumpPointer = -1
    }

    // ---------- update ----------
    private fun update(dt: Float) {
        if (platforms.isEmpty()) makeLevel()
        val accel = 1500f
        if (left) { vx -= accel * dt; facing = -1f }
        if (right) { vx += accel * dt; facing = 1f }
        if (!left && !right) vx *= .82f
        vx = vx.coerceIn(-300f, 300f)
        if (jumpQueued && onGround) { vy = -620f; onGround = false; sfx(0, 65); burst(playerX + 20f, playerY + 48f, 5, Color.WHITE) }
        jumpQueued = false
        vy = (vy + 1550f * dt).coerceAtMost(850f)
        val oldY = playerY
        playerX += vx * dt; playerY += vy * dt
        playerX = playerX.coerceIn(0f, worldWidth - 45f)
        onGround = false
        for (p in platforms) {
            if (playerX + 34f > p.x && playerX + 4f < p.x + p.w && oldY + 48f <= p.y + 10f && playerY + 48f >= p.y && vy >= 0f) {
                playerY = p.y - 48f; vy = 0f; onGround = true
            }
        }
        invul = (invul - dt).coerceAtLeast(0f)
        for (e in enemies) {
            if (!e.alive) continue
            e.x += e.dir * (if (e.flying) 95f else 65f) * dt
            if (e.x < e.minX || e.x > e.maxX) e.dir *= -1f
            if (abs(playerX - e.x) < 34f && abs(playerY - e.y) < 36f) {
                if (vy > 80f && playerY < e.y) {
                    e.alive = false; vy = -350f; burst(e.x, e.y, 12, K(0xFFFFEB3B)); sfx(1, 80)
                } else hurt()
            }
        }
        if (bossX > 0f && bossHp > 0) {
            bossX += (if (playerX < bossX) -1f else 1f) * 48f * dt
            bossX = bossX.coerceIn(worldWidth - 570f, worldWidth - 230f)
            bossFlash = (bossFlash - dt).coerceAtLeast(0f)
            if (abs(playerX - bossX) < 48f && abs(playerY - (groundY - 58f)) < 58f) {
                if (vy > 80f && playerY < groundY - 75f && bossFlash <= 0f) {
                    bossHp--; vy = -420f; bossFlash = .55f; burst(bossX, groundY - 65f, 16, K(0xFFFFD05A)); sfx(2, 130)
                    if (bossHp <= 0) { burst(bossX, groundY - 65f, 30, K(0xFFFF8A45)); bossX = -1f; sfx(1, 220) }
                } else if (bossFlash <= 0f) hurt()
            }
        }
        for (c in coins) if (!c.taken && abs(playerX + 20f - c.x) < 28f && abs(playerY + 22f - c.y) < 35f) {
            c.taken = true; coinsNow++; burst(c.x, c.y, 7, K(0xFFFFD34E)); sfx(1, 55)
        }
        for (p in particles) { p.x += p.vx * dt; p.y += p.vy * dt; p.vy += 360f * dt; p.life -= dt }
        particles.removeAll { it.life <= 0f }
        for (z in hazards) if (playerX + 32f > z.x && playerX < z.x + z.w && playerY + 45f > z.y) hurt()
        if (playerY > h + 150f) hurt()
        if (onGround && playerX > checkX + 400f) { checkX = playerX; checkY = playerY }
        if (playerX > goalX && bossHp <= 0) winLevel()
        camera = (playerX - w * .35f).coerceIn(0f, (worldWidth - w).coerceAtLeast(0f))
    }

    private fun winLevel() {
        screen = 4; inputLock = .6f
        left = false; right = false; jumpQueued = false
        leftPointer = -1; rightPointer = -1; jumpPointer = -1
        unlocked = max(unlocked, minOf(world * 20 + level + 1, 241))
        bestCoins += coinsNow
        prefs.edit().putInt("unlocked", unlocked).putInt("coins", bestCoins).apply()
    }

    private fun hurt() {
        if (invul > 0f) return
        hp--
        sfx(3, 120)
        burst(playerX + 20f, playerY + 20f, 10, K(0xFFFF5252))
        if (hp <= 0) hp = 3
        playerX = checkX; playerY = checkY - 4f
        vx = 0f; vy = 0f; invul = 1.4f
    }

    // ---------- game drawing ----------
    private fun drawGame() {
        val wd = worlds[world]
        bg(world, camera)
        val cv = art.cv
        cv?.save(); cv?.translate(-camera, 0f)
        // lantai bawah dunia (jurang berwarna gelap di dasar)
        for (p in platforms) if (p.x + p.w > camera - 20f && p.x < camera + w + 20f) scenes.platform(wd, p)
        for (z in hazards) scenes.spike(z, time)
        var ci = 0
        for (c in coins) { if (!c.taken) scenes.coin(c.x, c.y, time, ci * .7f); ci++ }
        for (e in enemies) if (e.alive) scenes.enemy(wd, e, time)
        if (bossX > 0f && bossHp > 0) scenes.boss(wd, bossX, groundY, bossFlash > 0f, bossHp, time)
        scenes.flag(wd, goalX, groundY, time)
        val blink = (time * 1.3f) % 3f > 2.9f
        if (invul <= 0f || ((time * 14f).toInt() % 2 == 0)) scenes.hero(playerX, playerY, vx, vy, facing, time, blink)
        for (p in particles) {
            val al = (255f * (p.life / .55f).coerceIn(0f, 1f)).toInt()
            art.circle((al shl 24) or (p.color and 0xFFFFFF), p.x, p.y, 4.5f)
        }
        cv?.restore()
        drawHud()
        controlPad(w * .115f, h * .87f, h * .115f, 0)
        controlPad(w * .235f, h * .87f, h * .115f, 1)
        controlPad(w * .88f, h * .84f, h * .15f, 2)
        art.button("II", w * .92f, h * .04f, w * .06f, h * .1f, K(0xFFFFD36E), K(0xFFFF8F1F))
    }

    private fun drawHud() {
        panel(w * .02f, h * .035f, w * .3f, h * .15f)
        art.titleText("WORLD ${world + 1} • LEVEL $level", w * .035f, h * .085f, h * .038f, Color.WHITE, OUT)
        for (i in 0 until 3) {
            val on = i < hp
            art.heart(if (on) K(0xFFFF4D6D) else K(0x55FFFFFF), w * .042f + i * h * .06f, h * .125f, h * .02f)
            if (on) art.circle(K(0x99FFFFFF), w * .042f + i * h * .06f - h * .01f, h * .12f, h * .005f)
        }
        coinIcon(w * .042f + 3.5f * h * .06f + w * .01f, h * .13f, h * .022f)
        art.titleText("$coinsNow", w * .042f + 3.5f * h * .06f + w * .028f, h * .152f, h * .042f, K(0xFFFFE066), OUT)
        if (bossHp > 0) art.titleText("BOSS $bossHp/4", w * .4f, h * .085f, h * .05f, K(0xFFFF5A57), OUT)
    }

    private fun controlPad(cx: Float, cy: Float, r: Float, kind: Int) {
        val pressed = (kind == 0 && left) || (kind == 1 && right) || (kind == 2 && jumpPointer != -1)
        val rr = if (pressed) r * .93f else r
        art.circle(K(0x55000000), cx + 3f, cy + 6f, rr)
        art.circle(OUT, cx, cy, rr + 3f)
        val top = if (kind == 2) K(0xFFFFC04A) else K(0xFF6EC6FF)
        val bot = if (kind == 2) K(0xFFFF7A1A) else K(0xFF2A7BE0)
        art.radial(top, bot, cx, cy - rr * .15f, rr * 1.15f)
        art.oval(K(0x55FFFFFF), cx, cy - rr * .5f, rr * .6f, rr * .3f)
        when (kind) {
            0 -> art.poly(Color.WHITE, cx + rr * .3f, cy - rr * .42f, cx - rr * .38f, cy, cx + rr * .3f, cy + rr * .42f)
            1 -> art.poly(Color.WHITE, cx - rr * .3f, cy - rr * .42f, cx + rr * .38f, cy, cx - rr * .3f, cy + rr * .42f)
            else -> {
                art.poly(Color.WHITE, cx - rr * .45f, cy + rr * .05f, cx, cy - rr * .5f, cx + rr * .45f, cy + rr * .05f)
                art.rect(Color.WHITE, cx - rr * .17f, cy, rr * .34f, rr * .42f, 4f)
            }
        }
    }

    private fun drawPause() {
        art.rect(K(0x99000000), 0f, 0f, w, h)
        panel(w * .28f, h * .15f, w * .44f, h * .68f)
        art.titleText("PAUSED", w * .5f, h * .33f, h * .09f, Color.WHITE, OUT, true)
        art.button("RESUME", w * .35f, h * .42f, w * .3f, h * .13f, K(0xFFFFC04A), K(0xFFFF7A1A))
        art.button("LEVEL MAP", w * .35f, h * .62f, w * .3f, h * .13f, K(0xFF6EC6FF), K(0xFF2A7BE0))
    }

    private fun drawWin() {
        bg(world)
        for (i in 0 until 18) {
            val a = time * .5f + i * .35f
            art.star(intArrayOf(K(0xFFFFEB3B), K(0xFFFF80AB), K(0xFF69F0AE), K(0xFF40C4FF))[i % 4],
                w * .5f + cos(a + i) * w * (.15f + (i % 5) * .06f), h * .35f + sin(a * 1.3f + i) * h * (.2f + (i % 3) * .05f), 6f + i % 4 * 3f, a)
        }
        panel(w * .2f, h * .12f, w * .6f, h * .72f)
        art.titleText("LEVEL COMPLETE!", w * .5f, h * .30f, h * .1f, K(0xFFFFE066), OUT, true)
        art.text("Great job, little hero!", w * .5f, h * .40f, h * .05f, Color.WHITE, false, 0, true)
        for (i in 0 until 3) art.star(K(0xFFFFD54F), w * .4f + i * w * .1f, h * .5f + (if (i == 1) -h * .02f else 0f), h * .055f, sin(time * 2f + i) * .2f)
        coinIcon(w * .38f, h * .62f, h * .03f)
        art.titleText("+$coinsNow COINS", w * .42f, h * .645f, h * .06f, K(0xFFFFE066), OUT)
        art.button("NEXT LEVEL", w * .34f, h * .68f, w * .32f, h * .13f, K(0xFFA5FF8A), K(0xFF2EBF4F))
        art.button("LEVEL MAP", w * .03f, h * .04f, w * .19f, h * .1f, K(0xFF6EC6FF), K(0xFF2A7BE0))
    }

    // ---------- input ----------
    private fun inCircle(x: Float, y: Float, cx: Float, cy: Float, r: Float) = (x - cx) * (x - cx) + (y - cy) * (y - cy) <= r * r
    private fun inRect(x: Float, y: Float, rx: Float, ry: Float, rw: Float, rh: Float) = x >= rx && x <= rx + rw && y >= ry && y <= ry + rh

    private fun startLevel() { makeLevel(); screen = 3; paused = false; inputLock = .3f }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val idx = e.actionIndex
        val x = e.getX(idx); val y = e.getY(idx)
        if (screen == 3) {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    if (paused) {
                        if (inRect(x, y, w * .35f, h * .42f, w * .3f, h * .13f)) paused = false
                        else if (inRect(x, y, w * .35f, h * .62f, w * .3f, h * .13f)) { paused = false; screen = 2; platforms.clear() }
                        return true
                    }
                    if (inRect(x, y, w * .9f, 0f, w * .1f, h * .18f)) { paused = true; left = false; right = false; return true }
                    val id = e.getPointerId(idx)
                    if (inCircle(x, y, w * .115f, h * .87f, h * .16f)) { leftPointer = id; left = true }
                    else if (inCircle(x, y, w * .235f, h * .87f, h * .16f)) { rightPointer = id; right = true }
                    else if (x > w * .6f && y > h * .45f) { jumpPointer = id; jumpQueued = true }
                }
                MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                    val id = e.getPointerId(idx)
                    if (id == leftPointer) { leftPointer = -1; left = false }
                    if (id == rightPointer) { rightPointer = -1; right = false }
                    if (id == jumpPointer) jumpPointer = -1
                }
                MotionEvent.ACTION_CANCEL -> {
                    left = false; right = false; leftPointer = -1; rightPointer = -1; jumpPointer = -1
                }
            }
            return true
        }
        if (e.actionMasked != MotionEvent.ACTION_UP || inputLock > 0f) return true
        when (screen) {
            0 -> {
                if (inRect(x, y, w * .1f, h * .56f, w * .27f, h * .14f)) { world = unlockedWorld; level = unlockedLevel; screen = 2 }
                else if (inRect(x, y, w * .1f, h * .74f, w * .27f, h * .11f)) screen = 1
            }
            1 -> {
                if (inRect(x, y, w * .03f, h * .04f, w * .15f, h * .1f)) screen = 0
                else for (i in 0..unlockedWorld) {
                    val r = worldRect(i)
                    if (inRect(x, y, r[0], r[1], r[2], r[3])) { world = i; screen = 2; break }
                }
            }
            2 -> {
                if (inRect(x, y, w * .03f, h * .04f, w * .15f, h * .1f)) { screen = 1 }
                else for (i in 1..20) {
                    val r = levelRect(i)
                    if (inRect(x, y, r[0], r[1], r[2], r[3]) && world * 20 + i <= unlocked) { level = i; startLevel(); break }
                }
            }
            4 -> {
                if (inRect(x, y, w * .03f, h * .04f, w * .19f, h * .1f)) screen = 2
                else if (inRect(x, y, w * .34f, h * .68f, w * .32f, h * .13f)) {
                    if (level < 20) { level++; startLevel() }
                    else if (world < 11) { world++; level = 1; startLevel() }
                    else screen = 2
                }
            }
        }
        return true
    }
}
