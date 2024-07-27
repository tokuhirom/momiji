package io.github.tokuhirom.momiji.ipadic

import io.github.tokuhirom.momiji.core.character.CharMap
import io.github.tokuhirom.momiji.core.dict.Dict
import io.github.tokuhirom.momiji.core.matrix.Matrix

/**
 * Load the dictionary.
 */
actual fun MomijiIpadicLoader.loadSysDic(): Dict = momijiLoadSysDic()

/**
 * Load the matrix of the transition cost.
 */
actual fun MomijiIpadicLoader.loadMatrix(): Matrix = momijiLoadMatrix()

/**
 * Load the character map.
 */
actual fun MomijiIpadicLoader.loadCharMap(): CharMap = momijiLoadCharMap()

/**
 * Load the unknown word dictionary.
 */
actual fun MomijiIpadicLoader.loadUnknown(): Dict = momijiLoadUnknown()

fun main() {
    val loader = MomijiIpadicLoader()
    val engine = loader.load()
    val lattice = engine.buildLattice("東京都")
    lattice.viterbi().forEachIndexed { index, node ->
        val transitionCost =
            node.minPrev?.let { prev ->
                engine.costManager.getTransitionCost(prev, node)
            } ?: 0

        println(
            "$index transition=$transitionCost emission=${node.dictRow?.token?.wcost} ${node.surface} ${node.dictRow?.feature}",
        )
    }
}
