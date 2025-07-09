package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.hooks

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.watabou.noosa.Game
import com.watabou.utils.GameMath
import kotlin.math.sign

class AnimationState(
    startingState: Boolean,
) {
    var state: Boolean = startingState
    var progress: Float = targetForState(startingState)
    var easingUp: ((Float) -> Float)? = null
    var easingDown: ((Float) -> Float)? = null

    inline fun <T> animate(
        targetState: Boolean,
        durationSeconds: Float,
        crossinline animation: (progress: Float) -> T,
    ): T {
        val target = targetForState(targetState)

        var easing: ((Float) -> Float)? = null
        var progress = this.progress
        if (progress != target) {
            val step = (1f / durationSeconds) * Game.elapsed * sign(target - progress)

            easing = if (step > 0) easingUp else easingDown

            progress = GameMath.gate(0f, progress + step, 1f)
        }

        if (progress == target) {
            this.state = targetState
        }

        this.progress = progress

        return animation(easing?.let { it(progress) } ?: progress)
    }

    fun done(targetState: Boolean): Boolean = progress == targetForState(targetState)

    @Suppress("NOTHING_TO_INLINE")
    @PublishedApi
    internal inline fun targetForState(targetState: Boolean): Float = if (targetState) 1f else 0f
}

inline fun <reified T : Any> Ui.useAnimation(
    tracker: Any,
    state: Boolean,
    durationSeconds: Float,
    crossinline animation: (progress: Float) -> T,
): T =
    use {
        val hook = get<AnimationState>(tracker).getOrInitWith { AnimationState(state) }

        hook.animate(state, durationSeconds, animation)
    }

class LoopingState {
    var progress: Float = 0f

    var repeats: Int = 0

    fun active(): Boolean = progress > 0f

    fun paused(): Boolean = progress < 0f

    inline fun <T> animate(
        running: Boolean,
        durationSeconds: Float,
        pauseInSeconds: Float,
        crossinline animation: (progress: Float) -> T,
    ): T {
        val reset = 1f + pauseInSeconds / durationSeconds
        while (progress > 1f) {
            repeats += 1
            progress -= reset
            if (!running) {
                progress = 0f
            }
        }

        val step = (1f / durationSeconds) * Game.elapsed
        // finish the loop if it's not running
        if (running || progress > 0f) {
            progress += step
        }

        return animation(GameMath.gate(0f, progress, 1f))
    }
}

inline fun <reified T : Any> Ui.useLooping(
    tracker: Any,
    running: Boolean,
    durationSeconds: Float,
    pauseInSeconds: Float = 0f,
    crossinline animation: (progress: Float) -> T,
): T =
    use {
        val state by get<LoopingState>(tracker).also { it.getOrInit(LoopingState()) }
        state.animate(running, durationSeconds, pauseInSeconds, animation)
    }

class TweenState {
    private var startValue: Float = 0f
    private var target: Float = 0f

    private var progress: Float = 0f
    private var duration: Float = 0f

    var value: Float = 0f
        private set

    var easing: ((Float) -> Float)? = null

    fun animate(
        target: Float,
        durationSeconds: Float,
    ): Float {
        if (this.target != target || this.duration != durationSeconds) {
            this.target = target
            this.duration = durationSeconds
            this.progress = 0f
            this.startValue = value
        }

        val elapsed = Game.elapsed
        val progress = GameMath.gate(0f, elapsed / durationSeconds, 1f)

        val easedProgress = easing?.let { it(progress) } ?: progress
        value =
            when {
                progress <= 0f -> {
                    startValue
                }

                progress >= 1f -> {
                    target
                }

                else -> {
                    startValue + (target - startValue) * easedProgress
                }
            }

        return value
    }

    fun setInstant(target: Float): Float {
        this.target = target
        this.progress = 1f
        this.startValue = target
        this.value = target
        return value
    }
}

fun Ui.useTweened(
    tracker: Any,
    value: Float,
    durationSeconds: Float,
): Float =
    use {
        val hook = get<TweenState>(tracker).getOrInitWith { TweenState() }

        hook.animate(value, durationSeconds)
    }
