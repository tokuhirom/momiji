package io.github.tokuhirom.momiji.core

import io.github.tokuhirom.momiji.core.matrix.Matrix

/**
 * Cost manager
 */
data class CostManager(
    private val matrix: Matrix,
) {
    /**
     * Get the emission cost
     */
    fun getEmissionCost(node: Node): Int = node.dictRow?.cost ?: 0

    /**
     * Get the transition cost
     *
     * @param left the left node
     * @param right the right node
     */
    fun getTransitionCost(
        left: Node,
        right: Node,
    ): Short {
        val leftRightId =
            when (left) {
                is Node.BOS -> 0
                is Node.EOS -> error("Should not reach here")
                is Node.Word -> left.dictRow?.rightId ?: return 0
            }
        val rightLeftId =
            when (right) {
                is Node.BOS -> error("Should not reach here")
                is Node.EOS -> 0
                is Node.Word -> right.dictRow?.leftId ?: return 0
            }

        return matrix.find(leftRightId, rightLeftId)
    }
}
