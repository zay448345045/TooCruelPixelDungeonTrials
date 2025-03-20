package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui

import com.watabou.noosa.NinePatch
import kotlin.math.roundToInt

data class Vec2(
    val x: Int,
    val y: Int,
) {
    operator fun plus(other: Vec2): Vec2 = Vec2(x + other.x, y + other.y)

    operator fun minus(other: Vec2): Vec2 = Vec2(x - other.x, y - other.y)

    operator fun times(other: Int): Vec2 = Vec2(x * other, y * other)

    fun scaleRounding(factor: Float): Vec2 = Vec2((x * factor).roundToInt(), (y * factor).roundToInt())

    fun scaleTruncating(factor: Float): Vec2 = Vec2((x * factor).toInt(), (y * factor).toInt())
}

data class Pos2(
    val x: Int,
    val y: Int,
) {
    operator fun plus(other: Vec2): Pos2 = Pos2(x + other.x, y + other.y)

    operator fun minus(other: Vec2): Pos2 = Pos2(x - other.x, y - other.y)

    operator fun minus(other: Pos2): Vec2 = Vec2(x - other.x, y - other.y)
}

@ExposedCopyVisibility
data class Rect private constructor(
    val min: Pos2,
    val max: Pos2,
) {
    companion object {
        fun fromMinMax(
            min: Pos2,
            max: Pos2,
        ): Rect = Rect(min, max)

        fun fromMinMax(
            minX: Int,
            minY: Int,
            maxX: Int,
            maxY: Int,
        ): Rect = Rect(Pos2(minX, minY), Pos2(maxX, maxY))

        fun fromSize(
            pos: Pos2,
            size: Vec2,
        ): Rect = Rect(pos, pos + size)

        val ZERO = Rect(Pos2(0, 0), Pos2(0, 0))

        val UNBOUNDED =
            Rect(
                Pos2(Int.MIN_VALUE, Int.MIN_VALUE),
                Pos2(Int.MAX_VALUE, Int.MAX_VALUE),
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
        val min =
            Pos2(
                kotlin.math.max(min.x, other.min.x),
                kotlin.math.max(min.y, other.min.y),
            )
        val max =
            Pos2(
                kotlin.math.min(max.x, other.max.x),
                kotlin.math.min(max.y, other.max.y),
            )
        if (min.x > max.x || min.y > max.y) {
            return null
        }
        return fromMinMax(min, max)
    }

    /**
     * Returns the smallest rectangle that contains both this rectangle and the other rectangle.
     */
    fun union(other: Rect): Rect {
        val min =
            Pos2(
                kotlin.math.min(min.x, other.min.x),
                kotlin.math.min(min.y, other.min.y),
            )
        val max =
            Pos2(
                kotlin.math.max(max.x, other.max.x),
                kotlin.math.max(max.y, other.max.y),
            )
        return fromMinMax(min, max)
    }

    fun centerInside(size: Vec2): Rect {
        val min =
            Pos2(
                left() + (width() - size.x) / 2,
                top() + (height() - size.y) / 2,
            )
        return fromMinMax(min, min + size)
    }

    fun width(): Int = max.x - min.x

    fun height(): Int = max.y - min.y

    fun size(): Vec2 = Vec2(width(), height())

    fun left(): Int = min.x

    fun right(): Int = max.x

    fun top(): Int = min.y

    fun bottom(): Int = max.y

    fun center(): Pos2 = Pos2((min.x + max.x) / 2, (min.y + max.y) / 2)

    fun centerLeft(): Pos2 = Pos2(min.x, (min.y + max.y) / 2)

    fun centerRight(): Pos2 = Pos2(max.x, (min.y + max.y) / 2)

    fun centerTop(): Pos2 = Pos2((min.x + max.x) / 2, min.y)

    fun centerBottom(): Pos2 = Pos2((min.x + max.x) / 2, max.y)

    fun topLeft(): Pos2 = min

    fun topRight(): Pos2 = Pos2(max.x, min.y)

    fun bottomLeft(): Pos2 = Pos2(min.x, max.y)

    fun bottomRight(): Pos2 = max

    fun shrink(amount: Int): Rect = shrink(amount, amount, amount, amount)

    fun shrink(
        horizontal: Int,
        vertical: Int,
    ): Rect = shrink(horizontal, vertical, horizontal, vertical)

    fun shrink(margins: Margins): Rect =
        shrink(
            margins.left,
            margins.top,
            margins.right,
            margins.bottom,
        )

    fun shrink(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ): Rect {
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
                max.y - bottom,
            )
        }
    }

    fun expand(amount: Int): Rect = expand(amount, amount, amount, amount)

    fun expand(
        horizontal: Int,
        vertical: Int,
    ): Rect = expand(horizontal, vertical, horizontal, vertical)

    fun expand(margins: Margins): Rect =
        expand(
            margins.left,
            margins.top,
            margins.right,
            margins.bottom,
        )

    fun expand(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ): Rect =
        fromMinMax(
            min.x - left,
            min.y - top,
            max.x + right,
            max.y + bottom,
        )

    fun scaleFromOrigin(factor: Int): Rect =
        fromMinMax(
            min.x * factor,
            min.y * factor,
            max.x * factor,
            max.y * factor,
        )
}

fun Rect?.contains(pos: Pos2): Boolean {
    if (this == null) {
        return false
    }
    return pos.x >= min.x && pos.x < max.x && pos.y >= min.y && pos.y < max.y
}

@JvmInline
value class ScreenRect(
    val rect: Rect,
)

data class Margins(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    companion object {
        fun same(value: Int): Margins = Margins(value, value, value, value)

        fun symmetric(
            horizontal: Int,
            vertical: Int,
        ): Margins = Margins(horizontal, vertical, horizontal, vertical)

        fun only(
            left: Int = 0,
            top: Int = 0,
            right: Int = 0,
            bottom: Int = 0,
        ): Margins = Margins(left, top, right, bottom)

        val ZERO = Margins(0, 0, 0, 0)
    }

    fun isZero(): Boolean = left == 0 && top == 0 && right == 0 && bottom == 0

    fun size(): Vec2 = Vec2(left + right, top + bottom)
}

fun NinePatch.margins(): Margins =
    Margins(
        marginLeft(),
        marginTop(),
        marginRight(),
        marginBottom(),
    )
