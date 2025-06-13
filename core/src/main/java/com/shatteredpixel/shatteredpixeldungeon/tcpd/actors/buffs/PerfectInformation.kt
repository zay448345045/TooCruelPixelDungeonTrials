package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping
import com.watabou.utils.Bundle

class PerfectInformation :
    Buff(),
    SwitchLevelBuff {
    private var wantMM = true

    override fun act(): Boolean {
        prolong(target, MindVision::class.java, MindVision.DURATION)
        prolong(target, Awareness::class.java, Awareness.DURATION)
        val hero = target as? Hero
        if (wantMM && hero != null) {
            val s = ScrollOfMagicMapping()
            s.anonymize()
            s.setCurrent(hero)
            ScrollOfMagicMapping().doRead()
            hero.spend(-Scroll.TIME_TO_READ)
            wantMM = false
        }
        spend(TICK)
        return true
    }

    fun pushBackInTime() {
        clearTime()
        spend(-TICK)
    }

    override fun onSwitchLevel() {
        pushBackInTime()
        wantMM = true
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(WANT_MM, wantMM)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        wantMM = bundle.getBoolean(WANT_MM)
    }

    companion object {
        const val WANT_MM = "want_magic_mapping"
    }
}
