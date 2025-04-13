package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.chars

import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

class ExtraCharData : Bundlable {
    var originalHT = -1

    override fun restoreFromBundle(bundle: Bundle) {
        originalHT = bundle.getInt(ORIGINAL_HT)
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(ORIGINAL_HT, originalHT)
    }

    companion object {
        const val TAG = "tcpd_data"

        private const val ORIGINAL_HT = "original_ht"
    }
}
