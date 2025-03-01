package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Pos2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Rect
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2

abstract class Layout(protected var availableSpace: Rect) {
    open class Horizontal(availableSpace: Rect) : Layout(availableSpace) {
        companion object: LayoutConstructor {
            override fun construct(availableSpace: Rect): Layout {
                return Horizontal(availableSpace)
            }
        }

        var cursor: Pos2 = availableSpace.min

        override fun allocate(desired: Vec2, spacing: Int): Rect {
            val rect = Rect.fromSize(cursor, desired).intersection(availableSpace)!!
            cursor = Pos2(rect.right() + spacing, cursor.y)
            return rect
        }

        override fun addSpace(amount: Int) {
            cursor = Pos2(cursor.x + amount, cursor.y)
        }

        override fun nextAvailableSpace(): Rect {
            return Rect.fromMinMax(cursor, availableSpace.max)
        }

        override fun childContinued(): LayoutConstructor {
            return Horizontal
        }
    }

    open class Vertical(availableSpace: Rect) : Layout(availableSpace) {
        companion object: LayoutConstructor {
            override fun construct(availableSpace: Rect): Layout {
                return Vertical(availableSpace)
            }
        }
        var cursor: Pos2 = availableSpace.min

        override fun allocate(desired: Vec2, spacing: Int): Rect {
            val rect = Rect.fromSize(cursor, desired).intersection(availableSpace)!!
            cursor = Pos2(cursor.x, rect.bottom() + spacing)
            return rect
        }

        override fun addSpace(amount: Int) {
            cursor = Pos2(cursor.x, cursor.y + amount)
        }

        override fun nextAvailableSpace(): Rect {
            return Rect.fromMinMax(cursor, availableSpace.max)
        }

        override fun childContinued(): LayoutConstructor {
            return Vertical
        }
    }

    open class Stack(availableSpace: Rect) : Layout(availableSpace) {
        companion object: LayoutConstructor {
            override fun construct(availableSpace: Rect): Layout {
                return Stack(availableSpace)
            }
        }
        override fun allocate(desired: Vec2, spacing: Int): Rect {
            return Rect.fromSize(availableSpace.min, desired).intersection(availableSpace)!!
        }

        override fun addSpace(amount: Int) {
            // no-op
        }

        override fun nextAvailableSpace(): Rect {
            return availableSpace
        }

        override fun childContinued(): LayoutConstructor {
            return Stack
        }
    }

    class VerticalJustified(availableSpace: Rect) : Vertical(availableSpace) {
        companion object: LayoutConstructor {
            override fun construct(availableSpace: Rect): Layout {
                return VerticalJustified(availableSpace)
            }
        }

        override fun allocate(desired: Vec2, spacing: Int): Rect {
            return super.allocate(Vec2(availableSpace.width(), desired.y), spacing)
        }

        override fun childContinued(): LayoutConstructor {
            return VerticalJustified
        }
    }

    /**
     * Allocates space for the next widget.
     */
    abstract fun allocate(desired: Vec2, spacing: Int): Rect

    /**
     * Adds the space before the next widget.
     */
    abstract fun addSpace(amount: Int)

    /**
     * Returns the available space for the next widget.
     */
    abstract fun nextAvailableSpace(): Rect

    /**
     * Returns the child layout with the same direction as this layout, whenever it
     * makes sense.
     */
    abstract fun childContinued(): LayoutConstructor

    fun getFullAvailableSpace(): Rect {
        return availableSpace
    }
}

interface LayoutConstructor {
    fun construct(availableSpace: Rect): Layout
}
