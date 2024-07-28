package io.github.tokuhirom.momiji.ipadic

import io.github.tokuhirom.momiji.core.character.CharMap
import io.github.tokuhirom.momiji.core.dict.Dict
import io.github.tokuhirom.momiji.core.matrix.Matrix
import io.github.tokuhirom.momiji.ipadic.char.CHAR
import io.github.tokuhirom.momiji.ipadic.matrix.MATRIX
import io.github.tokuhirom.momiji.ipadic.sys.SYS
import io.github.tokuhirom.momiji.ipadic.unk.UNK
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun momijiLoadSysDic(): Dict = Dict.parseBinary(Base64.decode(SYS))

/**
 * Load the matrix of the transition cost.
 */
@OptIn(ExperimentalEncodingApi::class)
fun momijiLoadMatrix(): Matrix = Matrix.parseBinary(Base64.decode(MATRIX))

/**
 * Load the character map.
 */
@OptIn(ExperimentalEncodingApi::class)
fun momijiLoadCharMap(): CharMap = CharMap.parseBinary(Base64.decode(CHAR))

/**
 * Load the unknown word dictionary.
 */
@OptIn(ExperimentalEncodingApi::class)
fun momijiLoadUnknown(): Dict = Dict.parseBinary(Base64.decode(UNK))
