package com.shatteredpixel.shatteredpixeldungeon.tcpd

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.RacingTheDeath


fun Mob.mobDamageHook(dmg: Int, src: Any): Int {
    if(Modifier.CARDINAL_DISABILITY.active()) {
        if(alignment == Char.Alignment.ENEMY) {
            val selfPos = Dungeon.level.cellToPoint(pos)
            val heroPos = Dungeon.level.cellToPoint(Dungeon.hero.pos)
            if(selfPos.x != heroPos.x && selfPos.y != heroPos.y) {
                return 0
            }
        }
    }

    return dmg
}

fun Hero.heroLiveHook() {
    if(Modifier.RACING_THE_DEATH.active()) {
        Buff.affect(this, RacingTheDeath::class.java)
    }
}
fun Hero.heroSpendConstantHook(time: Float) {
    if(time > 0) {
        buff(RacingTheDeath::class.java)?.tick()
    }
}