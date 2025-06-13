package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.mobs.StoredHeapData
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle

class HoldingHeap :
    Buff(),
    OnDeathEffectBuff {
    private var heap: StoredHeapData = StoredHeapData()

    fun set(heap: StoredHeapData): HoldingHeap {
        if (this.heap.isNothing()) {
            this.heap = heap
        } else {
            heap.mergeInto(this.heap)
        }

        return this
    }

    fun transformItems(cb: (Item) -> Item?) {
        heap.transformItems { cb(it) }
    }

    fun heap(): StoredHeapData = heap

    override fun icon(): Int {
        if (heap.items.isEmpty() && heap.childHeaps.isEmpty()) return BuffIndicator.NONE
        return BuffIndicator.NOINV
    }

    override fun desc(): String =
        if (heap.items.size > 0 && heap.childHeaps.size > 0) {
            Messages.get(this, "desc_both", heap.items.size, heap.childHeaps.size)
        } else if (heap.items.size > 0) {
            Messages.get(this, "desc_items", heap.items.size)
        } else if (heap.childHeaps.size > 0) {
            Messages.get(this, "desc_heaps", heap.childHeaps.size)
        } else {
            Messages.get(this, "desc_empty")
        }

    override fun onDeathProc() {
        val effects = mutableListOf<PersistHeapNestingBuff>()
        for (buff in target.buffs()) {
            if (buff is PersistHeapNestingBuff) {
                effects.add(buff)
            }
        }
        heap.restoreAtPos(
            Dungeon.level,
            target.pos,
            ignoredChars = listOf(target),
            spawnedCharEffects = effects,
            spawnPassive = false,
        )
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(HEAP, heap)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        heap = bundle.get(HEAP) as StoredHeapData
    }

    companion object {
        const val HEAP = "heap"
    }
}
