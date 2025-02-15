package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.hooks

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui

inline fun <R> Ui.use(cb: HookState.() -> R): R {
    val id = this.top().id().with("hooks")

    val value = this.ctx().memory.getOrPut(id) {
        HookState()
    }

    if (value !is HookState) {
        throw IllegalStateException("Memory value is not a HookState at $id")
    }

    return cb(value)
}

inline fun <reified T : Any> Ui.useState(tracker: Any, init: () -> T): MutableHookRef<T> {
    return use {
        get<T>(tracker).apply { getOrInitWith(init) }
    }
}

inline fun <reified T : Any> Ui.useMemo(tracker: Any, init: () -> T): HookRef<T> {
    return use {
        get<T>(tracker).apply { getOrInitWith(init) }.immutable()
    }
}