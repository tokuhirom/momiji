package io.github.tokuhirom.momiji.core.unknown

import io.github.tokuhirom.kdary.result.CommonPrefixSearchResult
import io.github.tokuhirom.kdary.samples.momiji.engine.Lattice
import io.github.tokuhirom.momiji.core.character.CharMap
import io.github.tokuhirom.momiji.core.src.Dict
import kotlin.math.min

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
        var hasSingleWord = false
        charMap.resolve(src[i]).let { charInfo ->
            if (charInfo.invoke || results.isEmpty()) {
                val s =
                    if (charInfo.group) {
                        // make a new word by grouping the same character category
                        val m =
                            if (charInfo.length == 0) {
                                src.length - i
                            } else {
                                min(src.length - i, charInfo.length)
                            }
                        val last =
                            (0 until m).last {
                                val prevCharCategory = charMap.resolve(src[i + it])
                                prevCharCategory == charInfo
                            }
                        src.substring(
                            i,
                            i + last + 1, // +1 since this parameter is exclusive.
                        )
                    } else {
                        src.substring(
                            i,
                            i + 1, // +1 since this parameter is exclusive.
                        )
                    }

                if (!results
                        .map {
                            src
                                .substring(i)
                                .encodeToByteArray()
                                .copyOfRange(0, it.length)
                                .decodeToString()
                        }.contains(s)
                ) {
                    val category = charMap.categoryName(charInfo.defaultType)
                    unknown[category].forEach { wordEntry ->
                        lattice.insert(i, i + s.length, wordEntry)
                    }
                    if (s.length == 1) {
                        hasSingleWord = true
                    }
                }
            }
        }
        return hasSingleWord
    }
}
