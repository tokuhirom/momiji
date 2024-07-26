package io.github.tokuhirom.momiji.ipadic

import io.github.tokuhirom.momiji.core.CostManager
import io.github.tokuhirom.momiji.core.LatticeBuilder
import io.github.tokuhirom.momiji.core.character.CharMap
import io.github.tokuhirom.momiji.core.dict.Dict
import io.github.tokuhirom.momiji.core.matrix.Matrix
import io.github.tokuhirom.momiji.core.unknown.DefaultUnknownWordDetector
import io.github.tokuhirom.momiji.ipadic.char.CHAR
import io.github.tokuhirom.momiji.ipadic.sys.SYS
import io.github.tokuhirom.momiji.ipadic.unk.UNK
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class MomijiIpadicLoader {
    /**
     * Build the LatticeBuilder object from the bundled ipadic.
     */
    fun load(): LatticeBuilder {
        val sys = loadSysDic()
        val matrix = loadMatrix()
        val charMap = loadCharMap()
        val unknown = loadUnknown()

        val costManager = CostManager(matrix)
        val unknownWordDetector = DefaultUnknownWordDetector(charMap, unknown)
        return LatticeBuilder(sys, costManager, unknownWordDetector)
    }

    /**
     * Load the dictionary.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun loadSysDic(): Dict = Dict.parseBinary(Base64.decode(SYS))

    /**
     * Load the matrix of the transition cost.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun loadMatrix(): Matrix = Matrix.parseBinary(Base64.decode(io.github.tokuhirom.momiji.ipadic.matrix.Matrix))

    /**
     * Load the character map.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun loadCharMap(): CharMap = CharMap.parseBinary(Base64.decode(CHAR))

    /**
     * Load the unknown word dictionary.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun loadUnknown(): Dict = Dict.parseBinary(Base64.decode(UNK))
}

fun main() {
    val loader = MomijiIpadicLoader()
    val engine = loader.load()
    val lattice = engine.buildLattice("東京都")
    lattice.viterbi().forEachIndexed { index, node ->
        val transitionCost =
            node.minPrev?.let { prev ->
                engine.costManager.getTransitionCost(prev, node)
            } ?: 0

        println(node.dictRow?.token)
        println(
            "$index transition=$transitionCost emission=${node.dictRow?.token?.wcost} ${node.surface} ${node.dictRow?.feature}",
        )
    }
}
