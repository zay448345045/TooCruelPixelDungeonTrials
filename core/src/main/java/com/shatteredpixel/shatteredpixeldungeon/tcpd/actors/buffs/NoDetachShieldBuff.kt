package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff

open class NoDetachShieldBuff : ShieldBuff() {
    // logic edited slightly as buff should not detach
    @Suppress("NAME_SHADOWING")
    override fun absorbDamage(dmg: Int): Int {
        var dmg = dmg
        if (shielding() <= 0) return dmg

        if (shielding() >= dmg) {
            decShield(dmg)
            dmg = 0
        } else {
            dmg -= shielding()
            decShield(shielding())
        }
        return dmg
    }
}
