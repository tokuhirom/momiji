package io.github.tokuhirom.momiji.ipadic

import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.momiji.core.CostManager
import io.github.tokuhirom.momiji.core.LatticeBuilder
import io.github.tokuhirom.momiji.core.character.CharMap
import io.github.tokuhirom.momiji.core.dict.Dict
import io.github.tokuhirom.momiji.core.matrix.Matrix
import io.github.tokuhirom.momiji.core.unknown.DefaultUnknownWordDetector
import io.github.tokuhirom.momiji.ipadic.char.CHAR
import io.github.tokuhirom.momiji.ipadic.dictcsv.DICT_CSV
import io.github.tokuhirom.momiji.ipadic.kdary.KDARY_BASE64
import io.github.tokuhirom.momiji.ipadic.unk.UNK
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class MomijiIpadicLoader {
    /**
     * Build the LatticeBuilder object from the bundled ipadic.
     */
    fun load(): LatticeBuilder {
        val kdary = loadKdary()
        val dict = loadDict()
        val matrix = loadMatrix()
        val charMap = loadCharMap()
        val unknown = loadUnknown()

        val costManager = CostManager(matrix)
        val unknownWordDetector = DefaultUnknownWordDetector(charMap, unknown)
        return LatticeBuilder(kdary, dict, costManager, unknownWordDetector)
    }

    /**
     * Load the KDary object.
     * It's used for common prefix search.
     *
     * @return The KDary object.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun loadKdary(): KDary = KDary.fromByteArray(Base64.decode(KDARY_BASE64))

    /**
     * Load the dictionary.
     */
    fun loadDict(): Dict = Dict.parseText(DICT_CSV)

    /**
     * Load the matrix of the transition cost.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun loadMatrix(): Matrix = Matrix.parseBinary(Base64.decode(io.github.tokuhirom.momiji.ipadic.matrix.Matrix))

    /**
     * Load the character map.
     */
    fun loadCharMap(): CharMap = CharMap.parseText(CHAR)

    /**
     * Load the unknown word dictionary.
     */
    fun loadUnknown(): Dict = Dict.parseText(UNK)
}

/*
Sample code:

fun main() {
    val loader = MomijiIpadicLoader()
    val engine = loader.load()
    val lattice = engine.buildLattice("布団が吹っ飛んだ")
    lattice.viterbi().forEachIndexed { index, node ->
        val transitionCost =
            node.minPrev?.let { prev ->
                engine.costManager.getTransitionCost(prev, node)
            } ?: 0

        println(
            String.format(
                "%3d transition=%-10d emission=%-10d %-20s %s",
                index,
                transitionCost,
                node.dictRow?.cost,
                node.surface,
                node.dictRow?.annotations,
            ),
        )
    }
}
*/
