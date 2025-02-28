package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter

import com.watabou.noosa.Gizmo

class PaintCache {
    private val cache: MutableMap<Int, Pair<VisualElement, Gizmo>> = mutableMapOf()
    private var pointer: Int = 0

    internal fun restart() {
        // clean up elements that are no longer needed
        cache.iterator().forEach {
            if (it.key >= pointer) {
                it.value.second.remove()
            }
        }
        pointer = 0
    }

    internal fun advance(element: VisualElement): Gizmo {
        val cached = cache[pointer]
        val gizmo = element.asGizmo(cached)
        if(gizmo != cached?.second) {
            cached?.second?.destroy()
        }
        cache[pointer] = Pair(element, gizmo)
        pointer++
        return gizmo
    }
}