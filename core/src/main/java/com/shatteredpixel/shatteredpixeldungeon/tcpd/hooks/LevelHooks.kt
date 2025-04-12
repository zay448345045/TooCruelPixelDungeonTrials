package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.Statistics
import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WellWater
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue
import com.shatteredpixel.shatteredpixeldungeon.items.Generator
import com.shatteredpixel.shatteredpixeldungeon.items.Heap
import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade
import com.shatteredpixel.shatteredpixeldungeon.levels.Level
import com.shatteredpixel.shatteredpixeldungeon.levels.Level.Feeling
import com.shatteredpixel.shatteredpixeldungeon.levels.Level.TIME_TO_RESPAWN
import com.shatteredpixel.shatteredpixeldungeon.levels.Level.set
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.connection.ConnectionRoom
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.entrance.EntranceRoom
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.ExterminationItemLock
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.PATRON_SEED_BLESS
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.PatronSaintsBlob
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.findBlob
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.Exterminating
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.HoldingHeap
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.InvulnerableUntilSeen
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.MindVisionExtBuff
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.mobs.HolderMimic
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.mobs.StoredHeapData
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.mobs.transformItems
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.curseIfAllowed
import com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.level.applyDomainOfHell
import com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.level.applyOverTheEdge
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage
import com.watabou.noosa.Game
import com.watabou.utils.BArray
import com.watabou.utils.GameMath
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import com.watabou.utils.Reflection
import kotlin.math.max
import kotlin.math.min

fun RegularLevel.createItemsHook() {
    if (Modifier.HEAD_START.active() && Dungeon.depth == 1) {
        val bonus =
            if (Modifier.PRISON_EXPRESS.active()) {
                1
            } else {
                0
            }
        val nUpgrades = 2 + bonus - Dungeon.LimitedDrops.UPGRADE_SCROLLS.count
        val nStrength = 1 + bonus - Dungeon.LimitedDrops.STRENGTH_POTIONS.count
        repeat(nUpgrades) {
            drop(ScrollOfUpgrade(), placeItemPos())
            Dungeon.LimitedDrops.UPGRADE_SCROLLS.count++
        }
        repeat(nStrength) {
            drop(PotionOfStrength(), placeItemPos())
            Dungeon.LimitedDrops.STRENGTH_POTIONS.count++
        }
    }
}

fun Level.postCreateHook() {
    if (Modifier.DROUGHT.active()) {
        applyDrought()
    }
    if (Modifier.SECOND_TRY.active()) {
        applySecondTry()
    }
    if (Modifier.HOLY_WATER.active()) {
        applyHolyWater()
    }
    if (Modifier.LOFT.active()) {
        applyLoft()
    }
    if (Modifier.LOOT_PARADISE.active()) {
        applyLootParadise()
    }
    if (Modifier.CURSED.active()) {
        applyCursed()
    }
    if (Modifier.MIMICS.active()) {
        applyMimics()
    }
    if (Modifier.EXTERMINATION.active()) {
        applyExtermination()
    }
    if (Modifier.JACK_IN_A_BOX.active()) {
        applyJackInTheBox()
    }
    if (Modifier.BOXED.active()) {
        applyBoxed()
    }
    if (Modifier.THUNDERSTRUCK.active()) {
        applyThunderstruck()
    }
    if (Modifier.OVER_THE_EDGE.active()) {
        applyOverTheEdge()
    }
    if (Modifier.DOMAIN_OF_HELL.active()) {
        applyDomainOfHell()
    }
}

@Suppress("NAME_SHADOWING")
fun Level.updateBlockingFovHook(
    c: Char,
    modifiableBlocking: BooleanArray,
    blocking: BooleanArray?,
): BooleanArray? {
    var blocking = blocking
    if (c is Hero || c.alignment == Char.Alignment.ALLY) {
        if (Modifier.LOFT.active()) {
            blocking = blocking ?: initBlocking(modifiableBlocking)
            for (i in blocking.indices) {
                if (!blocking[i] && pit[i]) {
                    blocking[i] = true
                }
            }
        }
        if (Modifier.BULKY_FRAME.active()) {
            val shrouding = Modifier.SHROUDING_PRESENCE.active()
            var pos: Int
            blocking = blocking ?: initBlocking(modifiableBlocking)
            for (mob in mobs) {
                if (mob.alignment == Char.Alignment.ALLY) continue
                pos = mob.pos
                blocking[pos] = true
                if (shrouding) {
                    for (c in PathFinder.NEIGHBOURS8) {
                        blocking[pos + c] = true
                    }
                }
            }
        }
    }
    return blocking
}

fun Level.updateHeroMindFovHook(
    h: Hero,
    heroMindFov: BooleanArray,
) {
    val shadowFov = BooleanArray(length())
    for (mob in mobs) {
        var heroDistance = -1
        val pos = mob.pos
        for (buff in mob.buffs()) {
            if (buff is MindVisionExtBuff) {
                var radius = buff.revealRadius()
                if (radius <= 0) {
                    continue
                }
                if (radius > ShadowCaster.MAX_DISTANCE) {
                    radius = ShadowCaster.MAX_DISTANCE
                }
                val maxHeroDistance = buff.maxHeroDistance()
                if (maxHeroDistance >= 0 && heroDistance < 0) {
                    heroDistance = distance(pos, h.pos)
                }
                if (maxHeroDistance < 0 || maxHeroDistance < heroDistance) {
                    if (radius == 1) {
                        for (i in PathFinder.NEIGHBOURS9) {
                            heroMindFov[pos + i] = true
                        }
                    } else {
                        val w = width()
                        val x = pos % w
                        val y = pos / w
                        ShadowCaster.castShadow(x, y, w, shadowFov, losBlocking, radius)
                        BArray.or(heroMindFov, shadowFov, heroMindFov)
                    }
                }
            }
        }
    }
}

fun dungeonObserveHook(dist: Int) {
    val level = Dungeon.level
    for (mob in level.mobs) {
        var heroDistance = -1
        val pos = mob.pos
        for (buff in mob.buffs()) {
            if (buff is MindVisionExtBuff) {
                var radius = buff.revealRadius()
                if (radius <= 0) {
                    continue
                }
                if (radius > ShadowCaster.MAX_DISTANCE) {
                    radius = ShadowCaster.MAX_DISTANCE
                }
                val maxHeroDistance = buff.maxHeroDistance()
                if (maxHeroDistance >= 0 && heroDistance < 0) {
                    heroDistance = level.distance(pos, Dungeon.hero.pos)
                }

                if (maxHeroDistance < 0 || maxHeroDistance < heroDistance) {
                    if (radius == 1) {
                        BArray.or(
                            level.visited,
                            level.heroFOV,
                            pos - 1 - level.width(),
                            3,
                            level.visited,
                        )
                        BArray.or(level.visited, level.heroFOV, pos - 1, 3, level.visited)
                        BArray.or(
                            level.visited,
                            level.heroFOV,
                            pos - 1 + level.width(),
                            3,
                            level.visited,
                        )
                        GameScene.updateFog(pos, 2)
                        continue
                    }
                    val w = level.width()
                    val x = pos % w
                    val y = pos / w
                    val l = max(0, (x - dist))
                    val r = min((x + dist), (level.width() - 1))
                    val t = max(0, (y - dist))
                    val b = min((y + dist), (level.height() - 1))
                    val width = r - l + 1

                    var pos1 = l + t * Dungeon.level.width()
                    for (i in t..b) {
                        BArray.or(
                            level.visited,
                            level.heroFOV,
                            pos,
                            width,
                            Dungeon.level.visited,
                        )
                        pos1 += Dungeon.level.width()
                    }
                    GameScene.updateFog(pos, dist)
                }
            }
        }
    }
}

private var bypassTransitionlimitations = false

fun Level.activateTransitionHook(
    hero: Hero,
    transition: LevelTransition,
): Boolean {
    if (bypassTransitionlimitations) return true
    if (Modifier.EXTERMINATION.active()) {
        if (!Exterminating.exterminationDone(this)) return false
    }
    val crumbling = Modifier.CRUMBLED_STAIRS.active()
    val prison = Modifier.PRISON_EXPRESS.active()
    if (Statistics.amuletObtained) {
        if (crumbling && transition.destDepth >= Dungeon.depth) {
            Game.runOnRenderThread {
                GameScene.show(
                    WndMessage(
                        Messages.get(
                            Modifier::class.java,
                            "crumbled_stairs_no_distractions",
                        ),
                    ),
                )
            }
            return false
        }
    } else {
        if ((crumbling || (prison && Dungeon.depth <= 6)) && transition.type == LevelTransition.Type.REGULAR_ENTRANCE) {
            Game.runOnRenderThread {
                GameScene.show(
                    WndMessage(
                        Messages.get(
                            Hero::class.java,
                            "leave",
                        ),
                    ),
                )
            }
            return false
        }
    }
    return true
}

@Suppress("NAME_SHADOWING")
fun Level.respawnCooldownHook(cooldown: Float): Float {
    var cooldown = cooldown
    if (Modifier.RESURRECTION.active()) {
        return 1f
    } else if (Modifier.REPOPULATION.active()) {
        cooldown *= GameMath.gate(0f, 1.1f * mobCount().toFloat() / mobLimit() - 0.1f, 1f)
        if (Modifier.FRACTAL_HIVE.active()) {
            cooldown = min(cooldown, TIME_TO_RESPAWN / 2f)
        }
    }
    return cooldown
}

fun Level.transitionNow(
    type: LevelTransition.Type,
    force: Boolean,
) {
    val tr = getTransition(type)
    if (tr != null) {
        if (force) bypassTransitionlimitations = true
        activateTransition(Dungeon.hero, tr)
        bypassTransitionlimitations = false
    }
}

private fun Level.initBlocking(modifiableBlocking: BooleanArray): BooleanArray {
    System.arraycopy(
        Dungeon.level.losBlocking,
        0,
        modifiableBlocking,
        0,
        modifiableBlocking.size,
    )

    return modifiableBlocking
}

fun Level.placeDuplicatorTraps(trap: Class<out Trap>) {
    if (trap.isAnonymousClass) return
    val cells = randomDuplicatorTrapCells()
    Random.shuffle(cells)
    val nTraps = 2

    for (i in 0 until minOf(nTraps, cells.size)) {
        val pos = cells[i]
        val t = setTrap(Reflection.newInstance(trap), pos)
        val old = map[pos]
        set(pos, Terrain.TRAP, this)
        t.reveal()
        GameScene.updateMap(pos)
        if (Dungeon.level.heroFOV[pos]) {
            GameScene.discoverTile(pos, old)
            ScrollOfMagicMapping.discover(pos)
        }
    }
}

fun Level.randomDuplicatorTrapCells(): List<Int> {
    val cells: MutableList<Int> = mutableListOf()
    for (i in 0 until length()) {
        if (isValidDuplicatorTrapPos(i)) {
            cells.add(i)
        }
    }
    return cells
}

private fun Level.isValidDuplicatorTrapPos(pos: Int): Boolean {
    if (pos < 0 || pos >= length()) return false
    return when (map[pos]) {
        Terrain.EMPTY, Terrain.GRASS, Terrain.HIGH_GRASS, Terrain.EMBERS,
        Terrain.EMPTY_DECO, Terrain.EMPTY_SP, Terrain.INACTIVE_TRAP, Terrain.WATER,
        -> true

        else -> false
    }
}

private fun RegularLevel.placeItemPos(roomType: Class<out Room?>? = null): Int {
    val cell: Int =
        if (roomType != null) randomDropCellExposedHook(roomType) else randomDropCellExposedHook()

    furrowCell(cell)
    return cell
}

fun Level.furrowCell(cell: Int) {
    if (map[cell] == Terrain.HIGH_GRASS || map[cell] == Terrain.FURROWED_GRASS) {
        map[cell] = Terrain.GRASS
        losBlocking[cell] = false
    }
}

private fun Level.applySecondTry() {
    var barricades = 0
    for (i in 0 until length()) {
        if (map[i] == Terrain.LOCKED_DOOR) {
            set(i, Terrain.DOOR, this)
        }
        if (map[i] == Terrain.BARRICADE) {
            set(i, Terrain.EMBERS, this)
            barricades++
        }
    }

    var h: Heap
    for (c in heaps.keyArray()) {
        h = heaps.get(c)
        if (h.type == Heap.Type.FOR_SALE) continue
        for (item in ArrayList<Item>(h.items)) {
            // Only remove regular keys, not subclasses
            if (item.javaClass == Key::class.java) h.items.remove(item)
            if (item.unique) continue
            if (!guaranteedItems.contains(item)) h.items.remove(item)
            if (item is PotionOfLiquidFlame && barricades-- > 0) h.items.remove(item)
        }
        if (h.items.isEmpty()) {
            heaps.remove(c)
        }
    }

    for (blob in blobs.values) {
        if (blob is WellWater) {
            blob.fullyClear(this)
        }
    }
}

private fun Level.applyThunderstruck() {
    // Reveal all traps if THUNDERSTRUCK modifier is active, for fairness
    for (trap in traps.valueList()) {
        trap.reveal()
        if (map[trap.pos] == Terrain.SECRET_TRAP) {
            set(trap.pos, Terrain.TRAP, this)
        }
    }
}

private fun Level.applyHolyWater() {
    for (i in 0 until length()) {
        if (map[i] == Terrain.WATER) {
            Blob.seed(i, PATRON_SEED_BLESS, PatronSaintsBlob::class.java, this)
        }
    }
}

private fun Level.applyLoft() {
    for (i in 0 until length()) {
        if ((map[i] == Terrain.WALL || map[i] == Terrain.WALL_DECO) && insideMap(i)) {
            map[i] = Terrain.CHASM
        } else if (!insideMap(i)) {
            map[i] = Terrain.CHASM
        }
    }
    buildFlagMaps()
    cleanWalls()
}

private fun Level.applyLootParadise() {
    val validCells = mutableListOf<Int>()
    for (i in 0 until length()) {
        if (!solid[i] && !pit[i]) {
            validCells.add(i)
        }
    }
    for (t in transitions) {
        validCells.remove(t.cell())
    }
    Random.shuffle(validCells)
    val amount = defaultNItems() * 10
    for (i in 0 until amount) {
        if (validCells.size <= i) break
        val cell = validCells[i]

        val toDrop = Generator.random() ?: continue
        furrowCell(cell)
        drop(toDrop, cell)
    }
}

fun Level.applyCursed() {
    transformItems {
        it.curseIfAllowed(true)
        it
    }
}

fun Level.applyDrought() {
    val chance = 0.8f
    var terrain: Int
    for (i in 0 until length()) {
        terrain = map[i]
        val isGrass =
            terrain == Terrain.GRASS || terrain == Terrain.HIGH_GRASS || terrain == Terrain.FURROWED_GRASS

        if ((isGrass || terrain == Terrain.WATER) && Random.Float() < chance
        ) {
            if (isGrass) {
                map[i] = Terrain.EMBERS
            }
            map[i] = Terrain.EMPTY
        }
    }

    buildFlagMaps()
}

fun Level.applyMimics() {
    val allItems = Modifier.MIMICS_ALL.active()
    val grind = Modifier.MIMICS_GRIND.active()
    val iter = heaps.iterator()
    while (iter.hasNext()) {
        val h = iter.next()
        if (h.value.type == Heap.Type.CHEST || allItems) {
            StoredHeapData.transformHeapIntoMimic(
                this,
                h.value,
                extraLoot = grind,
                weakHolders = !grind,
            )
            iter.remove()
        }
    }
}

fun Level.applyJackInTheBox() {
    for (mob in mobs.toTypedArray()) {
        if (mob.properties().contains(Char.Property.BOSS) ||
            mob
                .properties()
                .contains(Char.Property.MINIBOSS) ||
            mob
                .properties()
                .contains(Char.Property.IMMOVABLE)
        ) {
            continue
        }

        val heapData = StoredHeapData.fromMob(mob)
        val newMob = HolderMimic()
        newMob.setLevel(Dungeon.scalingDepth())
        newMob.pos = mob.pos
        Buff.affect(newMob, HoldingHeap::class.java).set(heapData)

        if (mob.buff(Exterminating::class.java) != null) {
            Buff.affect(newMob, Exterminating::class.java)
        }

        mobs.remove(mob)
        mobs.add(newMob)
    }
}

fun Level.applyBoxed() {
    if (Dungeon.bossLevel()) return
    if (this !is RegularLevel) return

    for (room in rooms()) {
        if (room is EntranceRoom || room is ConnectionRoom) continue
        for (x in (room.left)..(room.right)) {
            cellLoop@for (y in (room.top)..(room.bottom)) {
                if (x > room.left + 1 && x < room.right - 1 && y > room.top + 1 && y < room.bottom - 1) {
                    continue
                }

                val i = x + y * width()
                if (solid[i] || pit[i] || !(passable[i] || avoid[i])) continue
                var nearSolid = false
                for (j in PathFinder.CIRCLE8.indices) {
                    val o = PathFinder.CIRCLE8[j]
                    val cell = i + o

                    if (solid[cell]) nearSolid = true

                    if (j % 2 == 1 && isDoor(cell)) {
                        continue@cellLoop
                    }
                }

                if (!nearSolid) continue
                if (findMob(i) != null) continue

                val mob = createMob()
                val heap = StoredHeapData.fromMob(mob)

                val mimic = HolderMimic()
                Buff.affect(mimic, HoldingHeap::class.java).set(heap)
                Buff.affect(mimic, InvulnerableUntilSeen::class.java)
                mimic.setLevel(Dungeon.scalingDepth())
                mimic.pos = i
                mobs.add(mimic)
            }
        }
    }
}

fun Level.applyExtermination() {
    // Don't exterminate on boss levels
    if (Dungeon.bossLevel()) return

    val exterminateItemHolders = Modifier.MIMICS.active()

    val requireReachable = Modifier.POSTPAID_LOOT.active()
    if (requireReachable) {
        PathFinder.buildDistanceMap(
            getTransition(null).cell(),
            BArray.or(passable, avoid, null),
        )
    }
    if (Modifier.POSTPAID_LOOT.active()) {
        val lock = ExterminationItemLock()
        blobs[ExterminationItemLock::class.java] = lock
        for (h in heaps.valueList()) {
            lock.lockItem(this, h)
        }

        for (mob in mobs.toTypedArray()) {
            if (mob is Mimic) lock.lockMimic(this, mob)
            if (mob is Statue) lock.lockStatue(this, mob)
        }
    }
    for (m in mobs) {
        if (!exterminateItemHolders && (m is Mimic || m is Statue)) continue
        if (requireReachable && PathFinder.distance[m.pos] == Integer.MAX_VALUE) continue
        Buff.affect(m, Exterminating::class.java)
    }
}

fun Level.isDoor(cell: Int): Boolean {
    val terrain = map[cell]
    return terrain == Terrain.DOOR ||
        terrain == Terrain.OPEN_DOOR ||
        terrain == Terrain.LOCKED_DOOR ||
        terrain == Terrain.SECRET_DOOR ||
        terrain == Terrain.CRYSTAL_DOOR
}

inline fun Level.transformItems(crossinline cb: (Item) -> Item?) {
    for (h in heaps) {
        h.value.transformItems(cb)
    }

    val replacementMobs = mutableMapOf<Mob, Mob>()
    for (mob in mobs) {
        mob.transformItems(cb)?.let {
            replacementMobs[mob] = it
        }
    }

    for (mob in replacementMobs) {
        mobs.remove(mob.key)
        mobs.add(mob.value)
    }

    findBlob<ExterminationItemLock>()?.transformItems { cb(it) }
}

fun Level.destroyWall(cell: Int) {
    val terrain = map[cell]
    if (terrain == Terrain.WALL ||
        terrain == Terrain.WALL_DECO ||
        terrain == Terrain.STATUE ||
        terrain == Terrain.STATUE_SP ||
        terrain == Terrain.SECRET_DOOR ||
        terrain == Terrain.CRYSTAL_DOOR ||
        terrain == Terrain.BOOKSHELF
    ) {
        strongDestroy(cell)
    }
}

fun Level.strongDestroy(
    cell: Int,
    replaceWith: Int = Terrain.EMBERS,
) {
    if (!insideMap(cell)) return
    set(cell, replaceWith)
    for (o in PathFinder.NEIGHBOURS4) {
        val n = cell + o
        val terrain = map[n]
        if (terrain == Terrain.DOOR || terrain == Terrain.OPEN_DOOR || terrain == Terrain.CRYSTAL_DOOR || terrain == Terrain.LOCKED_DOOR) {
            strongDestroy(n)
        }
    }
    destroy(cell)
}

fun Level.defaultNItems(): Int {
    // drops 3/4/5 items 60%/30%/10% of the time
    var nItems = 3 + Random.chances(floatArrayOf(6f, 3f, 1f))

    if (feeling == Feeling.LARGE) {
        nItems += 2
    }
    return nItems
}
