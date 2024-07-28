package io.github.tokuhirom.momiji.ipadic

import io.github.tokuhirom.momiji.core.CostManager
import io.github.tokuhirom.momiji.core.LatticeBuilder
import io.github.tokuhirom.momiji.core.character.CharMap
import io.github.tokuhirom.momiji.core.dict.Dict
import io.github.tokuhirom.momiji.core.matrix.Matrix
import io.github.tokuhirom.momiji.core.unknown.DefaultUnknownWordDetector

/**
 * Load the dictionary.
 */
internal expect fun MomijiIpadicLoader.loadSysDic(): Dict

/**
 * Load the matrix of the transition cost.
 */
internal expect fun MomijiIpadicLoader.loadMatrix(): Matrix

/**
 * Load the character map.
 */
internal expect fun MomijiIpadicLoader.loadCharMap(): CharMap

/**
 * Load the unknown word dictionary.
 */
internal expect fun MomijiIpadicLoader.loadUnknown(): Dict

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
}
