package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.mobs.StoredHeapData
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator

class HoldingHeap: Buff(), OnDeathEffectBuff {
    lateinit var heap: StoredHeapData

    fun set(heap: StoredHeapData): HoldingHeap {
        this.heap = heap
        return this
    }

    override fun icon(): Int {
        if(heap.items.isEmpty() && heap.childHeaps.isEmpty()) return BuffIndicator.NONE
        return BuffIndicator.NOINV
    }

    override fun desc(): String {
        return if(heap.items.size > 0 && heap.childHeaps.size > 0) {
            Messages.get(this, "desc_both", heap.items.size, heap.childHeaps.size)
        } else if(heap.items.size > 0) {
            Messages.get(this, "desc_items", heap.items.size)
        } else if(heap.childHeaps.size > 0) {
            Messages.get(this, "desc_heaps", heap.childHeaps.size)
        } else {
            Messages.get(this, "desc_empty")
        }
    }

    override fun onDeathProc() {
        heap.restoreAtPos(Dungeon.level, target.pos)
    }
}