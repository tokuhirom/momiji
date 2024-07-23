package io.github.tokuhirom.momiji.engine

import io.github.tokuhirom.momiji.engine.src.DictRow

sealed class Node(
    open val surface: String,
    open val length: Int,
    open val dictRow: DictRow?,
    // 最小コスト
    open var minCost: Int = Int.MAX_VALUE,
    // 最小コスト経路(直近のみ保存)
    open var minPrev: Node? = null,
) {
    class BOS : Node("__BOS__", 0, null)

    class EOS : Node("__EOS__", 0, null)

    data class Word(
        override val surface: String,
        override val length: Int,
        override val dictRow: DictRow?,
        override var minCost: Int = Int.MAX_VALUE,
        override var minPrev: Node? = null,
    ) : Node(surface, length, dictRow, minCost, minPrev)
}
