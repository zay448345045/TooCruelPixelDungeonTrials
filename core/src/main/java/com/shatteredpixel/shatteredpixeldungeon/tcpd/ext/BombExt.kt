package com.shatteredpixel.shatteredpixeldungeon.tcpd.ext

import com.shatteredpixel.shatteredpixeldungeon.items.bombs.ArcaneBomb
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb.DoubleBomb
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Firebomb
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.FlashBangBomb
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.FrostBomb
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.HolyBomb
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Noisemaker
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.RegrowthBomb
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.ShrapnelBomb
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.SmokeBomb
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.WoollyBomb
import com.watabou.utils.Random
import com.watabou.utils.Reflection

val ALL_BOMBS: HashMap<Class<out Bomb>, Float> = run {
    val chances = HashMap<Class<out Bomb>, Float>()
    chances[Bomb::class.java] = 4f
    chances[DoubleBomb::class.java] = 1f

    // based on crafting costs
    chances[FrostBomb::class.java] = 1f / (1f + 0f)
    chances[WoollyBomb::class.java] = 1f / (1f + 0f)

    chances[Firebomb::class.java] = 1f / (1f + 1f)
    chances[Noisemaker::class.java] = 1f / (1f + 1f)

    chances[SmokeBomb::class.java] = 1f / (1f + 2f)
    chances[FlashBangBomb::class.java] = 1f / (1f + 2f)

    chances[RegrowthBomb::class.java] = 1f / (1f + 3f)
    chances[HolyBomb::class.java] = 1f / (1f + 3f)

    chances[ArcaneBomb::class.java] = 1f / (1f + 6f)
    chances[ShrapnelBomb::class.java] = 1f / (1f + 6f)
    chances
}

val BOMBERMOB_BOMBS = HashMap<Class<out Bomb>, Float>().also { chances ->
    chances[Bomb::class.java] = 4f
    // just for visuals, no gameplay difference
    chances[DoubleBomb::class.java] = 1f

    chances[FrostBomb::class.java] = 1f
    // Woolly bomb is annoying, so it's less likely
    chances[WoollyBomb::class.java] = 1 / 10f

    chances[Firebomb::class.java] = 1 / 2f
    chances[Noisemaker::class.java] = 1 / 2f

    chances[SmokeBomb::class.java] = 1 / 3f
    chances[FlashBangBomb::class.java] = 1 / 3f

    // Regrowth bomb is positive, so it's much less likely
    chances[RegrowthBomb::class.java] = 1 / 100f
    chances[HolyBomb::class.java] = 1 / 4f

    chances[ArcaneBomb::class.java] = 1 / 7f


    // Shrapnel bomb is disabled for bombermobs
    chances[ShrapnelBomb::class.java] = Float.MIN_VALUE
}

fun randomBomb(): Bomb {
    return Reflection.newInstance(Random.chances(ALL_BOMBS))
}

fun bombermobBomb(): Bomb {
    return Reflection.newInstance(Random.chances(BOMBERMOB_BOMBS))
}