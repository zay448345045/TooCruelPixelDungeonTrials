package com.shatteredpixel.shatteredpixeldungeon.tcpd.ext

inline fun<T> T?.isSomeAnd(block: (T) -> Boolean): Boolean {
    return this != null && block(this)
}

inline fun<T> T?.isNoneOr(block: (T) -> Boolean): Boolean {
    return this == null || block(this)
}