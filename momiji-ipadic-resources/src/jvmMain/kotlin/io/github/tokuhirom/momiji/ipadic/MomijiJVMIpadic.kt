package io.github.tokuhirom.momiji.ipadic

import io.github.tokuhirom.momiji.core.character.CharMap
import io.github.tokuhirom.momiji.core.dict.Dict
import io.github.tokuhirom.momiji.core.matrix.Matrix

/**
 * Load the dictionary.
 */
fun momijiLoadSysDic(): Dict {
    object {}.javaClass.classLoader.getResourceAsStream("mecab-ipadic/sys.dic").use {
        return Dict.parseBinary(it.readBytes())
    }
}

/**
 * Load the matrix of the transition cost.
 */
fun momijiLoadMatrix(): Matrix {
    object {}.javaClass.classLoader.getResourceAsStream("mecab-ipadic/matrix.bin").use {
        return Matrix.parseBinary(it.readBytes())
    }
}

/**
 * Load the character map.
 */
fun momijiLoadCharMap(): CharMap {
    object {}.javaClass.classLoader.getResourceAsStream("mecab-ipadic/char.bin")!!.use {
        return CharMap.parseBinary(it.readBytes())
    }
}

/**
 * Load the unknown word dictionary.
 */
fun momijiLoadUnknown(): Dict {
    object {}.javaClass.classLoader.getResourceAsStream("mecab-ipadic/unk.dic").use {
        return Dict.parseBinary(it.readBytes())
    }
}
