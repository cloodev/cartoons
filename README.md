# Cartoon Quest — Android platformer starter

A native Android 2D platform game prototype built with Kotlin Canvas.

## Included
- Landscape Android game, touch controls (left, right, jump) and pause/level-map button.
- Animated cartoon hero, scrolling camera, clouds, platforms, coins, patrol/flying enemies, spikes, checkpoints and goal flag.
- 12 themed worlds × 20 levels (240 level slots), procedural level layouts with increasingly varied gaps/platforms.
- Lobby, world selection, level selection, completion screen and saved unlock/coin progress.

## Changelog v1.1
- FIX: kode tidak bisa dikompilasi (literal warna 0xAARRGGBB bertipe Long) -> diganti helper `K()`.
- FIX: tombol WORLDS di lobby tidak pernah bisa diklik (area sentuh tertimpa PLAY).
- FIX: layar "Level Complete" langsung loncat ke level berikutnya saat jari diangkat dari tombol game.
- FIX: respawn bisa jatuh terus ke jurang (soft-lock); sekarang respawn di platform checkpoint + invulnerable 1.4 dtk.
- FIX: musik tetap bunyi saat app di background; game auto-pause. Boss arena kini punya lantai.
- FIX: partikel tidak fade, hit-area tombol/world select salah, ToneGenerator bisa crash, JVM target Gradle, gradle wrapper & icon ditambahkan.
- GRAFIK: langit gradient, matahari/bulan, bukit parallax, dekorasi per dunia (salju, bara, gelembung, kristal, dll), karakter ber-outline kartun, musuh/boss/koin/spike/bendera beranimasi, HUD hati+koin, tombol berwarna, kartu dunia berwarna.

## Build APK lewat GitHub (tanpa Android Studio)
Setelah di-push ke GitHub, buka tab **Actions** → workflow **Build APK** → jalankan (atau tunggu otomatis setelah push) → unduh artifact `CartoonQuest-debug-apk`.
Untuk membuat halaman Release dengan APK siap unduh: `git tag v1.1 && git push origin v1.1`.
Jika build gagal, unduh artifact `build-reports` di halaman run yang gagal.

## Build an APK
1. Install Android Studio (Hedgehog or newer) on a computer.
2. Open this folder as an existing Gradle project.
3. Allow Gradle sync to finish (JDK 17 recommended).
4. Choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Install the generated APK from `app/build/outputs/apk/debug/app-debug.apk`.

The project targets Android 15 (API 35), minimum Android 6.0 (API 23).

## Notes
This is an initial playable foundation, not a finished commercial release. Art is drawn with Canvas primitives so it has no external asset/license dependency. Background music and polished sound effects are not bundled yet. The 240 stages are generated from deterministic world/level seeds, so they are distinct procedural layouts rather than 240 hand-authored campaigns.
