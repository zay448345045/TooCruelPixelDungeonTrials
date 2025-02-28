package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui

import com.watabou.noosa.NinePatch

data class Vec2(val x: Int, val y: Int)

operator fun Vec2.plus(other: Vec2): Vec2 {
    return Vec2(x + other.x, y + other.y)
}

operator fun Vec2.minus(other: Vec2): Vec2 {
    return Vec2(x - other.x, y - other.y)
}

operator fun Vec2.times(other: Int): Vec2 {
    return Vec2(x * other, y * other)
}

data class Pos2(val x: Int, val y: Int)

operator fun Pos2.plus(other: Vec2): Pos2 {
    return Pos2(x + other.x, y + other.y)
}

operator fun Pos2.minus(other: Vec2): Pos2 {
    return Pos2(x - other.x, y - other.y)
}

operator fun Pos2.minus(other: Pos2): Vec2 {
    return Vec2(x - other.x, y - other.y)
}

data class Rect private constructor(val min: Pos2, val max: Pos2) {
    companion object {
        fun fromMinMax(min: Pos2, max: Pos2): Rect {
            return Rect(min, max)
        }

        fun fromMinMax(minX: Int, minY: Int, maxX: Int, maxY: Int): Rect {
            return Rect(Pos2(minX, minY), Pos2(maxX, maxY))
        }

        fun fromSize(pos: Pos2, size: Vec2): Rect {
            return Rect(pos, pos + size)
        }

        val ZERO = Rect(Pos2(0, 0), Pos2(0, 0))

        val UNBOUNDED = Rect(
            Pos2(Int.MIN_VALUE, Int.MIN_VALUE),
            Pos2(Int.MAX_VALUE, Int.MAX_VALUE)
        )
    }

    /**
     * Returns the intersection of this rectangle and the other rectangle, or
     * null if they do not intersect.
     */
    fun intersection(other: Rect?): Rect? {
        if (other == null) {
            return null
        }
        val min = Pos2(
            kotlin.math.max(min.x, other.min.x),
            kotlin.math.max(min.y, other.min.y)
        )
        val max = Pos2(
            kotlin.math.min(max.x, other.max.x),
            kotlin.math.min(max.y, other.max.y)
        )
        if (min.x >= max.x || min.y >= max.y) {
            return null
        }
        return fromMinMax(min, max)
    }

    /**
     * Returns the smallest rectangle that contains both this rectangle and the other rectangle.
     */
    fun union(other: Rect): Rect {
        val min = Pos2(
            kotlin.math.min(min.x, other.min.x),
            kotlin.math.min(min.y, other.min.y)
        )
        val max = Pos2(
            kotlin.math.max(max.x, other.max.x),
            kotlin.math.max(max.y, other.max.y)
        )
        return fromMinMax(min, max)
    }

    fun width(): Int {
        return max.x - min.x
    }

    fun height(): Int {
        return max.y - min.y
    }

    fun size(): Vec2 {
        return Vec2(width(), height())
    }

    fun left(): Int {
        return min.x
    }

    fun right(): Int {
        return max.x
    }

    fun top(): Int {
        return min.y
    }

    fun bottom(): Int {
        return max.y
    }

    fun center(): Pos2 {
        return Pos2((min.x + max.x) / 2, (min.y + max.y) / 2)
    }

    fun centerLeft(): Pos2 {
        return Pos2(min.x, (min.y + max.y) / 2)
    }

    fun centerRight(): Pos2 {
        return Pos2(max.x, (min.y + max.y) / 2)
    }

    fun centerTop(): Pos2 {
        return Pos2((min.x + max.x) / 2, min.y)
    }

    fun centerBottom(): Pos2 {
        return Pos2((min.x + max.x) / 2, max.y)
    }

    fun topLeft(): Pos2 {
        return min
    }

    fun topRight(): Pos2 {
        return Pos2(max.x, min.y)
    }

    fun bottomLeft(): Pos2 {
        return Pos2(min.x, max.y)
    }

    fun bottomRight(): Pos2 {
        return max
    }

    fun shrink(amount: Int): Rect {
        return shrink(amount, amount, amount, amount)
    }

    fun shrink(horizontal: Int, vertical: Int): Rect {
        return shrink(horizontal, vertical, horizontal, vertical)
    }

    fun shrink(margins: Margins): Rect {
        return shrink(
            margins.left,
            margins.top,
            margins.right,
            margins.bottom
        )
    }

    fun shrink(left: Int, top: Int, right: Int, bottom: Int): Rect {
        val newWidth = width() - left - right
        val newHeight = height() - top - bottom
        if (newWidth < 0 && newHeight < 0) {
            val center = center()
            return fromMinMax(center, center)
        } else if (newWidth < 0) {
            return fromMinMax(centerTop(), centerBottom())
        } else if (newHeight < 0) {
            return fromMinMax(centerLeft(), centerRight())
        } else {
            return fromMinMax(
                min.x + left,
                min.y + top,
                max.x - right,
                max.y - bottom
            )
        }
    }

    fun expand(amount: Int): Rect {
        return expand(amount, amount, amount, amount)
    }

    fun expand(horizontal: Int, vertical: Int): Rect {
        return expand(horizontal, vertical, horizontal, vertical)
    }

    fun expand(margins: Margins): Rect {
        return expand(
            margins.left,
            margins.top,
            margins.right,
            margins.bottom
        )
    }

    fun expand(left: Int, top: Int, right: Int, bottom: Int): Rect {
        return fromMinMax(
            min.x - left,
            min.y - top,
            max.x + right,
            max.y + bottom
        )
    }

    fun scaleFromOrigin(factor: Int) : Rect {
        return fromMinMax(
            min.x * factor,
            min.y * factor,
            max.x * factor,
            max.y * factor
        )
    }
}

fun Rect?.contains(pos: Pos2): Boolean {
    if (this == null) {
        return false
    }
    return pos.x >= min.x && pos.x < max.x && pos.y >= min.y && pos.y < max.y
}

@JvmInline
value class ScreenRect(val rect: Rect)

data class Margins(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    companion object {
        fun same(value: Int): Margins {
            return Margins(value, value, value, value)
        }

        fun symmetric(horizontal: Int, vertical: Int): Margins {
            return Margins(horizontal, vertical, horizontal, vertical)
        }

        val ZERO = Margins(0, 0, 0, 0)
    }

    fun isZero(): Boolean {
        return left == 0 && top == 0 && right == 0 && bottom == 0
    }
}

fun NinePatch.margins(): Margins {
    return Margins(
        marginLeft(),
        marginTop(),
        marginRight(),
        marginBottom(),
    )
}