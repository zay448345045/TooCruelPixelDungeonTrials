package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

interface DamageAmplificationBuff {
    fun damageMultiplier(source: Any?): Float
}