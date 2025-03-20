package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding

class BloodbagBleeding : Bleeding() {
    fun add(amount: Float) {
        level += amount
    }
}
