package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.watabou.utils.Bundle

class RecursiveResizing :
    Buff(),
    PersistHeapNestingBuff {
    private var step = 1f

    fun set(step: Float): RecursiveResizing {
        this.step = step
        return this
    }

    override fun applyNestingEffect(target: Char) {
        affect(target, Resizing::class.java).multiply(step)
        affect(target, RecursiveResizing::class.java).set(step)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(STEP, step)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        step = bundle.getFloat(STEP)
    }

    companion object {
        const val STEP = "step"
    }
}
