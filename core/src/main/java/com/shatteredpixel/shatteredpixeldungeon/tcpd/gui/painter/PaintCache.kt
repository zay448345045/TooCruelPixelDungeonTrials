package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.context.TwoFrameMap
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.UiId
import com.watabou.noosa.Gizmo

class PaintCache {
    private val cache: TwoFrameMap<UiId, Pair<VisualElement, Gizmo>> = TwoFrameMap()

    internal fun restart() {
        val seen = mutableSetOf<Gizmo>()
        cache.values().forEach {
            if (!seen.add(it.second)) {
                throw IllegalStateException("Gizmo $it is duplicated in the cache")
            }
        }
        // clean up elements that are no longer needed
        cache.swapDestroying {
            it.second.destroy()
        }
    }

    internal fun destroy() {
        cache.values().forEach {
            it.second.destroy()
        }
    }

    internal fun get(
        id: UiId,
        element: VisualElement,
    ): Gizmo {
        val cached = cache.get(id)
        val gizmo = element.asGizmo(cached)
        if (gizmo != cached?.second) {
            cached?.second?.destroy()
        }
        cache.set(id, Pair(element, gizmo))
        return gizmo
    }
}
