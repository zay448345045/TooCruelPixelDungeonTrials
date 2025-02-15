package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.context.Context
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Margins
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Pos2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Rect
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Style
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter.Painter

class Ui(private val ctx: Context, availableSpace: Rect, painter: Painter) {
    private val stack: MutableList<UiStackItem>

    init {
        val rootUi = UiStackItem(
            availableSpace = availableSpace,
            layout = LayoutDirection.STACK,
            painter = painter,
            id = UiId(0),
            margins = Margins.ZERO,
        )

        stack = mutableListOf(rootUi)
    }

    fun top(): UiStackItem {
        return stack.last()
    }

    fun ctx(): Context {
        return ctx
    }

    fun stackSize(): Int {
        return stack.size
    }

    fun pushLayout(
        availableSpace: Rect? = null,
        layout: LayoutDirection? = null,
        margins: Margins? = null,
        id: UiId? = null,
        painter: Painter? = null,
        style: Style? = null,
    ): UiId {
        val parent = stack.last()
        val m = margins ?: Margins.ZERO;
        val item = UiStackItem(
            availableSpace = availableSpace ?: parent.remainingSpace().shrink(m),
            layout = layout ?: parent.layout,
            id = id ?: parent.nextAutoId(),
            painter = painter,
            style = style,
            margins = m,
            parent = parent,
        )
        stack.add(item)
        return item.id
    }

    inline fun <T> withLayout(
        availableSpace: Rect? = null,
        layout: LayoutDirection? = null,
        margins: Margins? = null,
        id: UiId? = null,
        painter: Painter? = null,
        style: Style? = null,
        block: () -> T
    ): InnerResponse<T> {
        val layoutId = pushLayout(availableSpace, layout, margins, id, painter, style)
        val inner = block()
        val response = popLayout(layoutId)
        return InnerResponse(inner, response)
    }

    fun popLayout(id: UiId): UiResponse {
        val removed = stack.removeLast()
        if (removed.id != id) {
            throw IllegalStateException("Popped layout with wrong ID: ${removed.id} != $id")
        }
        val allocatedSpace = removed.allocatedSpace.expand(removed.margins)
        val parent = stack.last()
        return parent.allocateSize(allocatedSpace.size())
    }
}

class UiStackItem(
    val availableSpace: Rect,
    val margins: Margins,
    val layout: LayoutDirection,
    val id: UiId,
    private var painter: Painter? = null,
    private var parent: UiStackItem? = null,
    private var style: Style? = null,
    private var cursor: Pos2 = availableSpace.min,
) {
    private var nextId: UiId = id.next()
    var allocatedSpace: Rect = Rect.fromMinMax(availableSpace.min, availableSpace.min)
        private set

    fun style(): Style {
        var style = this.style ?: parent?.style()
        if (style != null) {
            return style
        }
        // No parent style found, use our own
        style = Style()
        this.style = style
        return style
    }

    fun setStyle(style: Style) {
        this.style = style
    }

    fun painter(): Painter {
        val painter = this.painter ?: parent?.painter()
        if (painter == null) {
            throw IllegalStateException("UI tree doesn't have any painter")
        }
        return painter
    }

    /**
     * Returns the id of this UI
     */
    fun id(): UiId {
        return id
    }

    /**
     * Returns the automatic ID for the next placed element.
     */
    fun nextAutoId(): UiId {
        val id = this.nextId
        this.nextId = id.next()
        return id
    }

    /**
     * Adds the given amount of space to the cursor.
     * @param amount The amount of space to add.
     */
    fun addSpace(amount: Int) {
        cursor = layout.advanceCursor(cursor, amount)
    }

    /**
     * Allocates a rectangle of the given size and advances the cursor.
     * @param size The size of the rectangle to allocate.
     */
    fun allocateSize(size: Vec2): UiResponse {
        val rect = Rect.fromSize(cursor, size)
        allocatedSpace = allocatedSpace.union(rect)
        cursor = layout.advanceCursor(cursor, size, style().itemSpacing)
        return UiResponse(rect, nextAutoId())
    }

    fun remainingSpace(): Rect {
        return Rect.fromMinMax(cursor, availableSpace.max)
    }
}

@JvmInline
value class UiId(internal val id: Int) {
    fun next(): UiId {
        return with(1)
    }

    fun with(id: Any): UiId {
        return UiId(Pair(this.id, id).hashCode())
    }
}

interface Widget {
    fun ui(ui: Ui): UiResponse
}

data class UiResponse(val rect: Rect, val id: UiId)
data class InnerResponse<T>(val inner: T, val response: UiResponse);