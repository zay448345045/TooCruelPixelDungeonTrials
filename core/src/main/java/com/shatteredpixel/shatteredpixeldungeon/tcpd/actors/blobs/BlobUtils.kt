package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob
import com.shatteredpixel.shatteredpixeldungeon.levels.Level

fun Blob.evolveUnchanged(off: IntArray) {
    var cell: Int
    for (i in area.top - 1..area.bottom) {
        for (j in area.left - 1..area.right) {
            cell = j + i * Dungeon.level.width()
            if (Dungeon.level.insideMap(cell)) {
                off[cell] = cur[cell]
                volume += off[cell]
            }
        }
    }
}

inline fun <reified T : Blob> Level.findBlob(): T? = blobs[T::class.java] as T?
