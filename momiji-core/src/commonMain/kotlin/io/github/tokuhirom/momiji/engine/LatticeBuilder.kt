package io.github.tokuhirom.momiji.engine

import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.kdary.samples.momiji.engine.Lattice
import io.github.tokuhirom.momiji.engine.src.Dict
import io.github.tokuhirom.momiji.engine.unknown.UnknownWordDetector

/**
 * LatticeBuilder builds a lattice from a given input string.
 *
 * @property kdary The KDary object to use for common prefix search.
 * @property dict The dictionary to use for known words.
 * @property costManager The cost manager to use for calculating costs.
 * @property unknownWordDetector The unknown word detector to use for detecting unknown words.
 */
data class LatticeBuilder(
    private val kdary: KDary,
    private val dict: Dict,
    val costManager: CostManager,
    private val unknownWordDetector: UnknownWordDetector,
) {
    fun buildLattice(src: String): Lattice {
        val lattice = Lattice(src, costManager)

        for (i in src.indices) {
            val bytes = src.substring(i).toByteArray(Charsets.UTF_8)
            val results = kdary.commonPrefixSearch(bytes)

            var hasSingleWord = false
            results.forEach { word ->
                val s = bytes.decodeToString(0, word.length)
                dict[s].forEach { wordEntry ->
                    lattice.insert(i, i + s.length, wordEntry)
                }
                if (s.length == 1) {
                    hasSingleWord = true
                }
            }

            // 未知語処理
            hasSingleWord = hasSingleWord or unknownWordDetector.detect(src, i, results, lattice)

            if (!hasSingleWord) {
                // 1文字の単語がない場合は、1文字の未知語を追加する。
                lattice.insert(i, i + 1, null)
            }
        }

        return lattice
    }
}
