package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Pos2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2

enum class LayoutDirection {
    HORIZONTAL,
    VERTICAL,
    STACK;

    fun cross(): LayoutDirection {
        return when (this) {
            HORIZONTAL -> VERTICAL
            VERTICAL -> HORIZONTAL
            STACK -> STACK
        }
    }

    fun advanceCursor(cursor: Pos2, amount: Int): Pos2 {
        return when (this) {
            HORIZONTAL -> Pos2(cursor.x + amount, cursor.y)
            VERTICAL -> Pos2(cursor.x, cursor.y + amount)
            STACK -> cursor
        }
    }

    fun advanceCursor(cursor: Pos2, allocated: Vec2, spacing: Int): Pos2 {
        return when (this) {
            HORIZONTAL -> Pos2(cursor.x + allocated.x + spacing, cursor.y)
            VERTICAL -> Pos2(cursor.x, cursor.y + allocated.y + spacing)
            STACK -> cursor
        }
    }
}