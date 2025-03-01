package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets

import com.shatteredpixel.shatteredpixeldungeon.Chrome
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Margins
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.UiResponse
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter.ComponentConstructor
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter.Painter
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter.descriptor
import com.shatteredpixel.shatteredpixeldungeon.ui.Button
import com.watabou.noosa.ui.Component

class UiButton {
    @PublishedApi
    internal fun createPainter(ui: Ui): Pair<Painter, Interaction> {
        val painter = ui.top().painter().withComponent(UiButtonComponent.Companion);
        val group = painter.getGroup()
        val interaction = if (group == null) {
            Interaction.NONE
        } else {
            (group as UiButtonComponent).resetInputs()
        }
        return Pair(painter, interaction)
    }

    inline fun <T> show(ui: Ui, block: (interaction: Interaction) -> T): InteractiveResponse<T> {
        val (painter, interaction) = createPainter(ui)

        val layoutId = ui.pushLayout(painter = painter)
        val inner = block(interaction)
        val response = ui.popLayout(layoutId)
        return InteractiveResponse(interaction, response, inner)
    }
}

inline fun <T> Ui.customButton(block: (interaction: Interaction) -> T): InteractiveResponse<T> {
    return UiButton().show(this, block)
}

fun Ui.redButton(text: String, size: Int = 9): InteractiveResponse<Unit> {
    return redButton {
        label(text, size)
    }
}

inline fun <T> Ui.redButton(
    content: (interaction: Interaction) -> T
): InteractiveResponse<T> {
    return customButton { interaction ->
        vertical(background = Chrome.Type.RED_BUTTON.descriptor()) {
            margins(Margins(3,3,1,3)) {
                withRedButtonBackground(this, interaction, content)
            }.inner
        }.inner
    }
}

@PublishedApi
internal inline fun <T> withRedButtonBackground(
    ui: Ui,
    interaction: Interaction,
    content: (interaction: Interaction) -> T
): T {
    val bg = (ui.top().painter().getGroup() as NinePatchComponent).ninePatch
    if (bg != null) {
        if (interaction.isPointerDown) {
            bg.brightness(1.2f)
        } else {
            bg.resetColor()
        }
    }
    return content(interaction)
}

data class Interaction(
    val justClicked: Boolean,
    val justRightClicked: Boolean,
    val justMiddleClicked: Boolean,
    val justPointerDown: Boolean,
    val justPointerUp: Boolean,
    val isPointerDown: Boolean,
) {
    companion object {
        val NONE = Interaction(
            justClicked = false,
            justRightClicked = false,
            justMiddleClicked = false,
            justPointerDown = false,
            justPointerUp = false,
            isPointerDown = false,
        )
    }
}

data class InteractiveResponse<T>(
    val interaction: Interaction,
    val response: UiResponse,
    val inner: T
) {
    fun clicked(): Boolean {
        return interaction.justClicked
    }

    inline fun <T> onClick(block: () -> T): T? {
        return if (clicked()) {
            block()
        } else {
            null
        }
    }

    fun isPointerDown(): Boolean {
        return interaction.isPointerDown
    }
}

private class UiButtonComponent : Button() {
    companion object : ComponentConstructor {
        override fun construct(): Component {
            return UiButtonComponent()
        }

        override fun componentClass(): Class<out Component> {
            return UiButtonComponent::class.java
        }
    }

    private var interaction = Interaction.NONE

    fun resetInputs(): Interaction {
        val i = interaction
        interaction = Interaction.NONE.copy(isPointerDown = i.isPointerDown)
        return i
    }

    override fun onClick() {
        super.onClick()
        interaction = interaction.copy(justClicked = true)
    }

    override fun onMiddleClick() {
        super.onMiddleClick()
        interaction = interaction.copy(justMiddleClicked = true)
    }

    override fun onRightClick() {
        super.onRightClick()
        interaction = interaction.copy(justRightClicked = true)
    }

    override fun onPointerDown() {
        super.onPointerDown()
        interaction = interaction.copy(justPointerDown = true, isPointerDown = true)
    }

    override fun onPointerUp() {
        super.onPointerUp()
        interaction = interaction.copy(justPointerUp = true, isPointerDown = false)
    }

    override fun clear() {
        super.clear()
        add(hotArea)
    }
}