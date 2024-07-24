package io.github.tokuhirom.momiji.engine.unknown

import io.github.tokuhirom.kdary.result.CommonPrefixSearchResult
import io.github.tokuhirom.kdary.samples.momiji.engine.Lattice
import io.github.tokuhirom.momiji.engine.src.CharMap
import io.github.tokuhirom.momiji.engine.src.Dict
import kotlin.math.max

/**
 * DefaultUnknownWordDetector is the default implementation of UnknownWordDetector.
 */
class DefaultUnknownWordDetector(
    private val charMap: CharMap,
    private val unknown: Dict,
) : UnknownWordDetector {
    override fun detect(
        src: String,
        i: Int,
        results: List<CommonPrefixSearchResult>,
        lattice: Lattice,
    ): Boolean {
        // TODO: results に含まれているものが重複で未知語として登録されていそう。。。
        var hasSingleWord = false
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
        return hasSingleWord
    }
}
