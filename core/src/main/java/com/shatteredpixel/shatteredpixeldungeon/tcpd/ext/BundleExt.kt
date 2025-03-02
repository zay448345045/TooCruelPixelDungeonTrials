package com.shatteredpixel.shatteredpixeldungeon.tcpd.ext

import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

inline fun <reified T : Bundlable> Bundle.getBundlable(key: String): T {
    val item = get(key);
    if (item !is T) {
        throw IllegalArgumentException("Item with key $key is not of type ${T::class.simpleName}")
    }
    return item
}