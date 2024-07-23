package io.github.tokuhirom.momiji.engine

import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.kdary.samples.momiji.engine.Lattice
import io.github.tokuhirom.momiji.engine.src.CharMap
import io.github.tokuhirom.momiji.engine.src.Dict
import kotlin.math.max

data class LatticeBuilder(
    private val kdary: KDary,
    private val dict: Dict,
    val costManager: CostManager,
    private val charMap: CharMap,
    private val unknown: Dict,
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
            charMap.resolve(src[i])?.let { charCategory ->
                if (charCategory.alwaysInvoke == 1 || results.isEmpty()) {
                    if (charCategory.grouping == 1) {
                        // make a new word by grouping the same character category
                        val m = max(src.length - i, charCategory.length)
                        val last =
                            (0 until m).last {
                                val prevCharCategory = charMap.resolve(src[i + it])
                                prevCharCategory == charCategory
                            }
                        val s =
                            src.substring(
                                i,
                                i + last + 1, // +1 since this parameter is exclusive.
                            )
                        unknown[charCategory.name].forEach { wordEntry ->
                            lattice.insert(i, i + last + 1, wordEntry)
                        }
                        if (s.length == 1) {
                            hasSingleWord = true
                        }
                    } else {
                        unknown[charCategory.name].forEach { wordEntry ->
                            lattice.insert(i, i + 1, wordEntry)
                        }
                        hasSingleWord = true
                    }
                }
            }

            if (!hasSingleWord) {
                // 1文字の単語がない場合は、1文字の未知語を追加する。
                lattice.insert(i, i + 1, null)
            }
        }

        return lattice
    }
}