package io.github.tokuhirom.momiji.core

import io.github.tokuhirom.momiji.core.dict.DictNode

/**
 * Node in the lattice.
 */
sealed class Node(
    open val surface: String,
    open val length: Int,
    open val dictRow: DictNode?,
    // 最小コスト
    open var minCost: Int = Int.MAX_VALUE,
    // 最小コスト経路(直近のみ保存)
    open var minPrev: Node? = null,
) {
    /**
     * BOS node
     */
    class BOS : Node("__BOS__", 0, null) {
        override fun toString(): String = "Node.BOS()"
    }

    /**
     * EOS node
     */
    class EOS : Node("__EOS__", 0, null) {
        override fun toString(): String = "Node.EOS()"
    }

    /**
     * Word node
     */
    data class Word(
        override val surface: String,
        override val length: Int,
        override val dictRow: DictNode?,
        override var minCost: Int = Int.MAX_VALUE,
        override var minPrev: Node? = null,
    ) : Node(surface, length, dictRow, minCost, minPrev)
}
