package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks

import com.shatteredpixel.shatteredpixeldungeon.levels.Level
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier

fun Level.postCreateHook() {
    // Reveal all traps if THUNDERSTRUCK modifier is active, for fairness
    if(Modifier.THUNDERSTRUCK.active()) {
        for(trap in traps.valueList()) {
            trap.reveal()
        }
    }
}