package com.cartoonquest.game

class Platform(val x: Float, val y: Float, val w: Float, val h: Float = 22f)
class Enemy(var x: Float, var y: Float, val minX: Float, val maxX: Float, var dir: Float = 1f,
            val flying: Boolean = false) { var alive = true }
class Coin(val x: Float, val y: Float, var taken: Boolean = false)
class Hazard(val x: Float, val y: Float, val w: Float)
class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float, val color: Int)

/** Satu dunia: warna langit (atas/bawah), rumput, tanah, aksen, bukit jauh/dekat, tema dekorasi. */
class World(
    val name: String, val skyTop: Int, val skyBottom: Int, val grass: Int, val dirt: Int,
    val accent: Int, val hillFar: Int, val hillNear: Int, val enemyColor: Int, val theme: Int
)

object Worlds {
    // theme: 0 hutan 1 jamur 2 gurun 3 salju 4 lava 5 permen 6 langit 7 hantu 8 laut 9 kristal 10 naga 11 gelap
    val all = listOf(
        World("Green Forest", K(0xFF4FC3F7), K(0xFFD6F5FF), K(0xFF5BD35F), K(0xFF9A6B3F), K(0xFFFFD34E), K(0xFF7FD0A0), K(0xFF3FAE6A), K(0xFF7BDB5A), 0),
        World("Mushroom Hills", K(0xFFFF9EC7), K(0xFFFFE3F0), K(0xFF8FE07A), K(0xFFB5714F), K(0xFFFF5C8A), K(0xFFE8A0D0), K(0xFFB06FD8), K(0xFFFF6B6B), 1),
        World("Desert Adventure", K(0xFFFFB347), K(0xFFFFEBB0), K(0xFFF2CC5A), K(0xFFC98A3C), K(0xFFFF7043), K(0xFFE8A55A), K(0xFFD9803A), K(0xFF4CAF50), 2),
        World("Frozen Land", K(0xFF7FD6F5), K(0xFFEAFBFF), K(0xFFF4FCFF), K(0xFF8EC9E8), K(0xFF29B6F6), K(0xFFB5E3F5), K(0xFF8AC4E6), K(0xFFE1F5FE), 3),
        World("Lava Kingdom", K(0xFFFF6E40), K(0xFFFFC66E), K(0xFF8D5A66), K(0xFF3E2631), K(0xFFFF3D00), K(0xFFC2573A), K(0xFF7A2E2E), K(0xFFFF5722), 4),
        World("Candy World", K(0xFFFF8AD8), K(0xFFFFE6F7), K(0xFFB8F27A), K(0xFFFF8FB8), K(0xFFFF2E93), K(0xFFFFB3E6), K(0xFFFF7EC8), K(0xFFFF4081), 5),
        World("Sky Islands", K(0xFF3AA8FF), K(0xFFCDEBFF), K(0xFF9BE86B), K(0xFFE8D2A5), K(0xFFFFEB3B), K(0xFFA6D8FF), K(0xFF70BDF5), K(0xFFFFFFFF), 6),
        World("Haunted Castle", K(0xFF5A4FA8), K(0xFFB8A8E8), K(0xFF8A6FC4), K(0xFF4A3C73), K(0xFFB388FF), K(0xFF7A6BC0), K(0xFF53459A), K(0xFFE8E0FF), 7),
        World("Ocean World", K(0xFF1CB5E0), K(0xFF9CF0E8), K(0xFF3FD0B5), K(0xFFE8C98A), K(0xFFFFD54F), K(0xFF4AC6C8), K(0xFF2A9FA8), K(0xFFFF7043), 8),
        World("Crystal Cave", K(0xFF3D4FA8), K(0xFF8A9BE8), K(0xFF52D9D0), K(0xFF3C4A8A), K(0xFF64FFDA), K(0xFF5A6BC4), K(0xFF3F4F9E), K(0xFFB388FF), 9),
        World("Dragon Valley", K(0xFFFF7A45), K(0xFFFFD28A), K(0xFFC9A84C), K(0xFF8A5A3A), K(0xFFFFC107), K(0xFFD9784A), K(0xFF9E4A2E), K(0xFFE53935), 10),
        World("Dark Kingdom", K(0xFF1E2250), K(0xFF5A5FA8), K(0xFF6F5FBF), K(0xFF2E2F55), K(0xFFFF4F88), K(0xFF3A3F80), K(0xFF272B63), K(0xFF8E24AA), 11)
    )
}
