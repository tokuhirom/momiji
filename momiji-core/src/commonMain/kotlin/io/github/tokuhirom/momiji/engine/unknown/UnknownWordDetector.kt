package io.github.tokuhirom.momiji.engine.unknown

import io.github.tokuhirom.kdary.result.CommonPrefixSearchResult
import io.github.tokuhirom.kdary.samples.momiji.engine.Lattice

// DefaultUnknownWordDetector で 99% のケースはカバーされている。
// しかしながら、DefaultUnknownWordDetector は Surrogate Pair に対応していない。
// Surrogate pair などにサポートする余地を残すために、UnknownWordDetector を interface として切る。

/**
 * getUnknownWords detects unknown words and adds them to the lattice.
 */
interface UnknownWordDetector {
    /**
     * Detect unknown words and add them to the lattice.
     *
     * @param src The input string.
     * @param i The current position in the input string.
     * @param results The results of the common prefix search.
     * @param lattice The lattice to add unknown words to.
     * @return True if a single word was detected, false otherwise.
     */
    fun detect(
        src: String,
        i: Int,
        results: List<CommonPrefixSearchResult>,
        lattice: Lattice,
    ): Boolean
}
