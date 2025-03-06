package com.shatteredpixel.shatteredpixeldungeon.tcpd.effects

import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter
import com.watabou.noosa.particles.Emitter

interface CustomBlobCellEmission {
    fun emit(emitter: BlobEmitter, factory: Emitter.Factory, index: Int, cellX: Int, cellY: Int, cell: Int, tileSize: Float)
}