package com.shatteredpixel.shatteredpixeldungeon.tcpd.utils

import com.watabou.utils.DeviceCompat

fun assertEq(a: Any, b: Any) {
    if(DeviceCompat.isDebug()) {
        if (a != b) {
            throw AssertionError("assertEq failed: $a != $b")
        }
    }
}