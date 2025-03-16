package com.shatteredpixel.shatteredpixeldungeon.utils

class BitPaint(private val width: Int) {
    private val coordinates: MutableSet<Int> = mutableSetOf()

    fun add(x: Int, y: Int) {
        coordinates.add(x + y * width)
    }

    fun addHLine(x1: Int, x2: Int, y: Int) {
        for (x in x1..x2) {
            add(x, y)
        }
    }

    fun addVLine(x: Int, y1: Int, y2: Int) {
        for (y in y1..y2) {
            add(x, y)
        }
    }

    fun addRect(x1: Int, y1: Int, x2: Int, y2: Int) {
        for (x in x1..x2) {
            for (y in y1..y2) {
                add(x, y)
            }
        }
    }

    fun toPairsArray(ox: Int, oy: Int): IntArray {
        val arr = IntArray(coordinates.size * 2)

        var i = 0
        for (coord in coordinates) {
            arr[i] = coord % width + ox
            arr[i + 1] = coord / width + oy
            i += 2
        }

        return arr
    }
}