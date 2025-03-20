package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.RetieredBuff

fun postInit() {
    Dungeon.hero.buff(RetieredBuff::class.java)?.processInventory(Dungeon.hero)
}
