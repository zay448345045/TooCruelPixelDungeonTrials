package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.getMap
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.putMap
import com.watabou.utils.Bundle

class TargetedResistance :
    Buff(),
    ResistanceBuff {
    private var resists: MutableMap<Class<*>, Float> = mutableMapOf()

    fun set(vararg resistances: Pair<Class<*>, Float>): TargetedResistance {
        for ((clazz, res) in resistances) {
            resists[clazz] = res
        }

        return this
    }

    override fun resist(effect: Class<*>): Float {
        for ((clazz, factor) in resists) {
            if (clazz.isAssignableFrom(effect)) {
                return factor
            }
        }
        return 1f
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        bundle.putMap(RESISTS, resists, { k, v -> put(k, v) }, { k, v -> put(k, v.toFloatArray()) })
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        resists =
            bundle
                .getMap(RESISTS, { getClassArray(it) }, { getFloatArray(it).toTypedArray() })
    }

    companion object {
        const val RESISTS = "resists"
    }
}
