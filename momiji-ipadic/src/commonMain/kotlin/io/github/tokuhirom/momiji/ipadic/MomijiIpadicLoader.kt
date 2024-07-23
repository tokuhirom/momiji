package io.github.tokuhirom.momiji.ipadic

import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.momiji.engine.CostManager
import io.github.tokuhirom.momiji.engine.LatticeBuilder
import io.github.tokuhirom.momiji.engine.src.CharMap
import io.github.tokuhirom.momiji.engine.src.Dict
import io.github.tokuhirom.momiji.engine.src.matrix.Matrix
import io.github.tokuhirom.momiji.ipadic.char.CHAR
import io.github.tokuhirom.momiji.ipadic.dictcsv.DICT_CSV
import io.github.tokuhirom.momiji.ipadic.kdary.KDARY_BASE64
import io.github.tokuhirom.momiji.ipadic.unk.UNK
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class MomijiIpadicLoader {
    @OptIn(ExperimentalEncodingApi::class)
    fun load(): LatticeBuilder {
        val bytes = Base64.decode(KDARY_BASE64)
        val kdary = KDary.fromByteArray(bytes)

        val dict = Dict.parse(DICT_CSV)

        val matrix = loadMatrix()

        val charMap = CharMap.parseText(CHAR)
        val unknown = Dict.parse(UNK)

        val costManager = CostManager(matrix)

        return LatticeBuilder(kdary, dict, costManager, charMap, unknown)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun loadMatrix(): Matrix = Matrix.parseBinary(Base64.decode(io.github.tokuhirom.momiji.ipadic.matrix.Matrix))

    fun loadCharMap(): CharMap = CharMap.parseText(CHAR)
}

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
