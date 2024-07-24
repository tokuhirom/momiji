package io.github.tokuhirom.momiji.engine.unknown

import io.github.tokuhirom.kdary.result.CommonPrefixSearchResult
import io.github.tokuhirom.kdary.samples.momiji.engine.Lattice
import io.github.tokuhirom.momiji.engine.src.CharMap
import io.github.tokuhirom.momiji.engine.src.Dict
import jdk.internal.org.jline.utils.Colors.s
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
        charMap.resolve(src[i])?.let { charCategory ->
            if (charCategory.alwaysInvoke == 1 || results.isEmpty()) {
                if (charCategory.grouping == 1) {
                    // make a new word by grouping the same character category
                    val m =
                        if (charCategory.length == null) {
                            src.length - i
                        } else {
                            min(src.length - i, charCategory.length)
                        }
                    val last =
                        (0 until m).last {
                            val prevCharCategory = charMap.resolve(src[i + it])
                            prevCharCategory == charCategory
                        }
                    val s: String =
                        src.substring(
                            i,
                            i + last + 1, // +1 since this parameter is exclusive.
                        )
                    if (!results.map { String(src.substring(i).toByteArray().copyOfRange(0, it.length)) }.contains(s)) {
                        unknown[charCategory.name].forEach { wordEntry ->
                            lattice.insert(i, i + last + 1, wordEntry)
                        }
                        if (s.length == 1) {
                            hasSingleWord = true
                        }
                    }
                } else {
                    val s: String =
                        src.substring(
                            i,
                            i + 1, // +1 since this parameter is exclusive.
                        )
                    if (!results.map { String(src.substring(i).toByteArray().copyOfRange(0, it.length)) }.contains(s)) {
                        unknown[charCategory.name].forEach { wordEntry ->
                            lattice.insert(i, i + 1, wordEntry)
                        }
                        hasSingleWord = true
                    }
                }
            }
        }
        return hasSingleWord
    }
}
