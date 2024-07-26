package io.github.tokuhirom.momiji.core

import io.github.tokuhirom.kdary.samples.momiji.engine.Lattice
import io.github.tokuhirom.momiji.core.dict.Dict
import io.github.tokuhirom.momiji.core.unknown.UnknownWordDetector

/**
 * LatticeBuilder builds a lattice from a given input string.
 *
 * @property kdary The KDary object to use for common prefix search.
 * @property dict The dictionary to use for known words.
 * @property costManager The cost manager to use for calculating costs.
 * @property unknownWordDetector The unknown word detector to use for detecting unknown words.
 */
data class LatticeBuilder(
    private val dict: Dict,
    val costManager: CostManager,
    private val unknownWordDetector: UnknownWordDetector,
) {
    fun buildLattice(src: String): Lattice {
        val lattice = Lattice(src, costManager)

        for (i in src.indices) {
            val bytes = src.substring(i).encodeToByteArray()
            val nodes = dict.commonPrefixSearch(bytes)

            var hasSingleWord = false
            nodes.forEach { node ->
                lattice.insert(
                    i,
                    i + node.surface.length,
                    node,
                )
                if (node.surface.length == 1) {
                    hasSingleWord = true
                }
            }

            // 未知語処理
            hasSingleWord = hasSingleWord or unknownWordDetector.detect(src, i, nodes, lattice)

            if (!hasSingleWord) {
                // 1文字の単語がない場合は、1文字の未知語を追加する。
                lattice.insert(i, i + 1, null)
            }
        }

        return lattice
    }
}
