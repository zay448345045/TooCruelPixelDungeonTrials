package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle


class BloodbagBleeding : Bleeding(){
    fun add(amount: Float) {
        level += amount
    }
}