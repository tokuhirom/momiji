package io.github.tokuhirom.momiji.ipadic

import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.momiji.dictcsv.DICT_CSV
import io.github.tokuhirom.momiji.engine.CostManager
import io.github.tokuhirom.momiji.engine.MomijiEngine
import io.github.tokuhirom.momiji.engine.src.CharMap
import io.github.tokuhirom.momiji.engine.src.Dict
import io.github.tokuhirom.momiji.engine.src.matrix.Matrix
import io.github.tokuhirom.momiji.ipadic.char.CHAR
import io.github.tokuhirom.momiji.ipadic.kdary.KDARY_BASE64
import io.github.tokuhirom.momiji.ipadic.matrix.MATRIX
import io.github.tokuhirom.momiji.ipadic.unk.UNK
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class MomijiIpadicLoader {
    @OptIn(ExperimentalEncodingApi::class)
    fun load(): MomijiEngine {
        val bytes = Base64.decode(KDARY_BASE64)
        val kdary = KDary.fromByteArray(bytes)

        val dict = Dict.parse(DICT_CSV)

        val matrix = Matrix.parse(MATRIX)

        val charMap = CharMap.parse(CHAR)
        val unknown = Dict.parse(UNK)

        val costManager = CostManager(matrix)

        return MomijiEngine(kdary, dict, costManager, charMap, unknown)
    }
}
