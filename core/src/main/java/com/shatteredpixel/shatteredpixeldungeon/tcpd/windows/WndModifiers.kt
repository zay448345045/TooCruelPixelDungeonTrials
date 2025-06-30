package com.shatteredpixel.shatteredpixeldungeon.tcpd.windows

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifiers
import com.shatteredpixel.shatteredpixeldungeon.tcpd.TCPDData
import com.shatteredpixel.shatteredpixeldungeon.tcpd.TCPDGameInfoData
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Trial
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Margins
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.TcpdComponent
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.TcpdWindow
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.hooks.AnimationState
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.hooks.useMemo
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.hooks.useState
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter.descriptor
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.PaginatedList
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.appearingIcon
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.columns
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.customButton
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.dimInactiveText
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.drawRedCheckbox
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.horizontal
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.iconButton
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.label
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.margins
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.redButton
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.rightToLeft
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.shrinkToFitLabel
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.verticalJustified
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.withRedButtonBackground
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.easeInBack
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.easeOutBack
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons
import com.shatteredpixel.shatteredpixeldungeon.ui.Window.TITLE_COLOR
import com.shatteredpixel.shatteredpixeldungeon.windows.WndError
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput

open class WndModifiers(
    private val modifiers: Modifiers,
    private val trial: Trial?,
    private val editable: Boolean,
) : TcpdWindow() {
    constructor(data: TCPDData) : this(data.modifiers, data.trial, false)
    constructor(data: TCPDGameInfoData) : this(data.modifiers, data.trials, false)

    init {
        maxSize = Vec2(120, (PixelScene.uiCamera.height * 0.9f).toInt())
    }

    override fun Ui.drawUi() {
        drawModifiers(modifiers, trial, editable)
    }
}

open class ModifiersComponent(
    private val modifiers: Modifiers,
    private val trial: Trial?,
    private val editable: Boolean,
) : TcpdComponent() {
    constructor(data: TCPDData) : this(data.modifiers, data.trial, false)
    constructor(data: TCPDGameInfoData) : this(data.modifiers, data.trials, false)

    override fun Ui.drawUi() {
        drawModifiers(modifiers, trial, editable)
    }
}

private fun Ui.drawModifiers(
    modifiers: Modifiers,
    trial: Trial?,
    editable: Boolean,
) {
    verticalJustified {
        rightToLeft {
            editStringButton(modifiers, editable)
            verticalJustified {
                label(Messages.get(WndModifiers::class.java, "title"), 12).widget.hardlight(
                    TITLE_COLOR,
                )
            }
        }

        if (trial != null) {
            shrinkToFitLabel(Messages.get(WndModifiers::class.java, "trial", trial.name), 9)
        }

        top().addSpace(2)

        var sort by useState(Unit) {
            FilterOptions(SortType.Default, false)
        }

        sort = sortButton(sort)

        val modifiersList by useMemo(sort) {
            val allEnabled = mutableListOf<Modifier>()
            Modifier.entries.filterTo(allEnabled) { modifiers.isEnabled(it) }.toMutableList()
            sort.apply(allEnabled)

            if (!editable) return@useMemo allEnabled

            val allDisabled = mutableListOf<Modifier>()
            Modifier.entries.filterTo(allDisabled) { !modifiers.isEnabled(it) }
            sort.apply(allDisabled)
            allEnabled + allDisabled
        }
        PaginatedList(modifiersList.size, 17).show(this) { i ->
            modifierBtn(modifiers, modifiersList[i], editable)
        }
    }
}

private fun Ui.modifierBtn(
    modifiers: Modifiers,
    modifier: Modifier,
    editable: Boolean,
) {
    rightToLeft {
        margins(Margins.only(top = 2)) {
            iconButton(Icons.INFO.descriptor()).onClick {
                ShatteredPixelDungeon.scene().add(
                    WndMessage(modifier.localizedDesc()),
                )
            }
        }
        verticalJustified {
            withEnabled(editable) {
                redButton {
                    val res =
                        shrinkToFitLabel(
                            modifier.localizedName(),
                            9,
                            availableSpace =
                                top()
                                    .nextAvailableSpace()
                                    .width() -
                                    Icons.CHECKED
                                        .descriptor()
                                        .size()
                                        .x,
                        )
                    drawRedCheckbox(modifiers.isEnabled(modifier), res.response.rect)
                }.onClick {
                    modifiers.toggle(modifier)
                }
            }
        }
    }
}

private fun Ui.editStringButton(
    modifiers: Modifiers,
    editable: Boolean,
) {
    iconButton(Icons.SCROLL_COLOR.descriptor()).onClick {
        ShatteredPixelDungeon.scene().add(
            object : WndTextInput(
                Messages.get(WndModifiers::class.java, "edit_title"),
                Messages.get(WndModifiers::class.java, "edit_body"),
                modifiers.serializeToString(),
                256,
                false,
                Messages.get(WndModifiers::class.java, "edit_set"),
                Messages.get(WndModifiers::class.java, "edit_clear"),
            ) {
                override fun onSelect(
                    positive: Boolean,
                    text: String,
                ) {
                    val textValue =
                        if (editable && !positive) {
                            ""
                        } else {
                            text
                        }
                    if (editable) {
                        try {
                            val trimmed = textValue.trim()
                            if (trimmed.isBlank()) {
                                modifiers.disableAll()
                            } else {
                                modifiers.enableFrom(
                                    Modifiers.deserializeFromString(
                                        trimmed,
                                    ),
                                )
                            }
                        } catch (e: Exception) {
                            ShatteredPixelDungeon.scene().add(
                                WndError(
                                    Messages.get(
                                        WndModifiers::class.java,
                                        "edit_error",
                                    ),
                                ),
                            )
                        }
                    }
                    super.onSelect(positive, textValue)
                }
            },
        )
    }
}

private fun Ui.sortButton(sort: FilterOptions): FilterOptions {
    var curSort = sort
    horizontal {
        rightToLeft {
            columns(floatArrayOf(1f, 1f), spacing = 1) {
                arrayOf(
                    SortType.Default,
                    SortType.Name,
                    SortType.New,
                    SortType.Completed,
                ).forEach { sortType ->
                    val selected = curSort.sortType == sortType
                    withEnabled(sortType.available()) {
                        customButton {
                            withRedButtonBackground(
                                this,
                                selected,
                                margins = Margins.ZERO,
                            ) {
                                horizontal {
                                    val arrow =
                                        appearingIcon(
                                            Icons.COMPASS.descriptor(),
                                            selected,
                                            easingDisappear = null,
                                        )

                                    val rotateAnim =
                                        ctx().getOrPutMemory(top().id.with("rotateAnim")) {
                                            AnimationState(curSort.sortReverse).also {
                                                it.easingUp = ::easeOutBack
                                                it.easingDown = ::easeInBack
                                            }
                                        }
                                    arrow.widget.hardlight(0f, 0f, 0f)
                                    arrow.widget.am = 1.5f
                                    arrow.widget.originToCenter()
                                    arrow.widget.angle =
                                        rotateAnim.animate(
                                            curSort.sortReverse,
                                            0.2f,
                                        ) { it * 180f }

                                    val label = label(sortType.localizedName(), 7)
                                    if (selected) {
                                        label.widget.hardlight(0xFFFF44)
                                    } else {
                                        label.widget.resetColor()
                                        dimInactiveText(label, sortType.available())
                                    }
                                }
                            }
                        }.onClick {
                            curSort =
                                if (selected) {
                                    FilterOptions(sortType, !curSort.sortReverse)
                                } else {
                                    FilterOptions(sortType, false)
                                }
                        }
                    }
                }
            }
        }
    }
    return curSort
}

private sealed interface SortType {
    fun sort(modifiers: MutableList<Modifier>)

    fun id(): String

    fun localizedName(): String = Messages.get(WndModifiers::class.java, "sort_${id()}")

    fun available(): Boolean = true

    data object Default : SortType {
        override fun sort(modifiers: MutableList<Modifier>) {}

        override fun id(): String = "default"
    }

    data object Name : SortType {
        override fun sort(modifiers: MutableList<Modifier>) {
            modifiers.sortBy {
                it.localizedName()
            }
        }

        override fun id(): String = "name"
    }

    data object New : SortType {
        override fun sort(modifiers: MutableList<Modifier>) {
            modifiers.sortByDescending {
                it.id
            }
        }

        override fun id(): String = "new"
    }

    data object Completed : SortType {
        override fun sort(modifiers: MutableList<Modifier>) {
            TODO()
        }

        override fun id(): String = "completed"

        override fun available(): Boolean = false
    }
}

private data class FilterOptions(
    val sortType: SortType,
    val sortReverse: Boolean,
) {
    fun apply(modifiers: MutableList<Modifier>) {
        sortType.sort(modifiers)
        if (sortReverse) {
            modifiers.reverse()
        }
    }
}
