package com.shatteredpixel.shatteredpixeldungeon.tcpd.ext

import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.levels.Level

fun Char.getFov(level: Level): BooleanArray {
    if (this.fieldOfView == null || this.fieldOfView.size != level.length()) {
        this.fieldOfView = BooleanArray(level.length())
    }
    return this.fieldOfView!!
}

fun Char.updateFov(level: Level): BooleanArray = this.getFov(level).also { level.updateFieldOfView(this, it) }

inline fun <reified T> Char.forEachBuff(cb: (T) -> Unit) {
    for (buff in buffs()) {
        if (buff is T) {
            cb(buff)
        }
    }
}
