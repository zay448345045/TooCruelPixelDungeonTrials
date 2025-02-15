package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Pos2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Rect
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2
import com.watabou.noosa.Camera

fun Camera.rect(): Rect {
    return Rect.fromSize(Pos2(x, y), Vec2(width, height))
}

fun Camera.withClip(rect: Rect?): Camera? {
    val newRect = camera.rect().intersection(rect) ?: return null
    return if (newRect == camera.rect()) {
        camera
    } else {
        Camera(newRect.min.x, newRect.min.y, newRect.width(), newRect.height(), this.zoom)
    }
}