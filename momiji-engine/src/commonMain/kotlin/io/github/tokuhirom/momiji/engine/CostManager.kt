package io.github.tokuhirom.momiji.engine

import io.github.tokuhirom.momiji.engine.src.matrix.Matrix

data class CostManager(
    private val matrix: Matrix,
) {
    /**
     * 生起コスト
     */
    fun getEmissionCost(node: Node): Int = node.dictRow?.cost ?: 0

    /**
     * 連接コスト
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
