package com.shatteredpixel.shatteredpixeldungeon.tcpd

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.getBundlable
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

class TCPDData : Bundlable {
    companion object {
        const val MODIFIERS = "modifiers"
    }

    fun asInfoData(): TCPDGameInfoData {
        return TCPDGameInfoData().also { it.modifiers = modifiers }
    }

    fun restoreFromInfoData(data: TCPDGameInfoData) {
        modifiers = data.modifiers
    }

    fun isChallenged(): Boolean {
        return modifiers.isChallenged()
    }

    var modifiers: Modifiers = SPDSettings.challenges()

    override fun restoreFromBundle(bundle: Bundle) {
        modifiers = bundle.getBundlable(MODIFIERS)
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(MODIFIERS, modifiers)
    }
}

class TCPDGameInfoData : Bundlable {
    companion object {
        const val MODIFIERS = "modifiers"
    }

    lateinit var modifiers: Modifiers

    fun isChallenged(): Boolean {
        return modifiers.isChallenged()
    }

    override fun restoreFromBundle(bundle: Bundle) {
        modifiers = bundle.getBundlable(MODIFIERS)
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(MODIFIERS, modifiers)
    }
}