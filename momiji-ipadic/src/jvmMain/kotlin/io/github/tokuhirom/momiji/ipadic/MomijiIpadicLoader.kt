package io.github.tokuhirom.momiji.ipadic

import io.github.tokuhirom.momiji.core.character.CharMap
import io.github.tokuhirom.momiji.core.dict.Dict
import io.github.tokuhirom.momiji.core.matrix.Matrix

/**
 * Load the dictionary.
 */
actual fun MomijiIpadicLoader.loadSysDic(): Dict {
    this.javaClass.classLoader.getResourceAsStream("mecab-ipadic/sys.dic").use {
        return Dict.parseBinary(it.readBytes())
    }
}

/**
 * Load the matrix of the transition cost.
 */
actual fun MomijiIpadicLoader.loadMatrix(): Matrix {
    this.javaClass.classLoader.getResourceAsStream("mecab-ipadic/matrix.bin").use {
        return Matrix.parseBinary(it.readBytes())
    }
}

/**
 * Load the character map.
 */
actual fun MomijiIpadicLoader.loadCharMap(): CharMap {
    this.javaClass.classLoader.getResourceAsStream("mecab-ipadic/char.bin")!!.use {
        return CharMap.parseBinary(it.readBytes())
    }
}

/**
 * Load the unknown word dictionary.
 */
actual fun MomijiIpadicLoader.loadUnknown(): Dict {
    this.javaClass.classLoader.getResourceAsStream("mecab-ipadic/unk.dic").use {
        return Dict.parseBinary(it.readBytes())
    }
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

        println(
            "$index transition=$transitionCost emission=${node.dictRow?.token?.wcost} ${node.surface} ${node.dictRow?.feature}",
        )
    }
}
