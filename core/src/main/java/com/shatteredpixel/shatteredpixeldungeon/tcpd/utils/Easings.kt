package com.shatteredpixel.shatteredpixeldungeon.tcpd.utils

import kotlin.math.pow

fun easeInOutBack(x: Float): Float {
    val c1 = 1.70158f
    val c2 = c1 * 1.525f

    return if (x < 0.5f) {
        ((2 * x).pow(2.0f) * ((c2 + 1) * 2 * x - c2)) / 2
    } else {
        ((2 * x - 2).pow(2.0f) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2
    }
}

fun easeOutBack(x: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1

    return 1 + c3 * (x - 1).pow(3) + c1 * (x - 1).pow(2)
}

fun easeInBack(x: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1

    return c3 * x.pow(3) - c1 * x.pow(2)
}

fun easeOutCubic(x: Float): Float = 1 - (1 - x).pow(3)

fun easeInCubic(x: Float): Float = x.pow(3)

fun easeInOutCubic(x: Float): Float =
    if (x < 0.5f) {
        4 * x.pow(3)
    } else {
        1 - (-2 * x + 2).pow(3) / 2
    }
