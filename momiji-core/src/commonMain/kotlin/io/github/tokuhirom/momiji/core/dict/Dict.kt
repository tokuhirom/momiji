package io.github.tokuhirom.momiji.core.dict

import io.github.tokuhirom.momiji.core.dict.DictRow.Companion.parseLine
import io.github.tokuhirom.momiji.core.utils.ByteReader

data class Dict(
    private val data: Map<String, List<DictRow>>,
) {
    val size
        get() = data.size

    operator fun get(s: String): List<DictRow> =
        data.getOrElse(s) {
            emptyList()
        }

    companion object {
        private const val DICTIONARY_MAGIC_ID: UInt = 0xef718f77u

        /**
         * Parse a dictionary.
         * The format is mecab's CSV.
         */
        fun parseText(src: String): Dict =
            Dict(
                src
                    .split("\n")
                    .filter {
                        it.isNotBlank()
                    }.map {
                        parseLine(it)
                    }.groupBy {
                        it.surface
                    },
            )

        fun parseBinary(bytes: ByteArray): Dict2 {
            val byteReader = ByteReader(bytes)

            val magic = byteReader.readUInt()
            val version = byteReader.readUInt()
            val type = byteReader.readUInt()
            val lexsize = byteReader.readUInt()
            val lsize = byteReader.readUInt()
            val rsize = byteReader.readUInt()
            val dsize = byteReader.readUInt()
            val tsize = byteReader.readUInt()
            val fsize = byteReader.readUInt()
            val dummy = byteReader.readUInt()

            check((DICTIONARY_MAGIC_ID xor magic).toInt() == bytes.size) {
                "Wrong magic. Broken dictionary"
            }
            println(bytes.size)
            check(byteReader.offset == 10 * 4)

            // 32 bytes
            val charset = byteReader.readNullFilledString(32)
            println(charset)
            check(byteReader.offset == 10 * 4 + 32)

            // Darts のバイナリが dsize バイトぶん入っている。
            // Darts の kotlin 実装はないので、使えないのでスキップ。。
            // KDary 作るときに Darts-clone じゃなくて Darts を移植すればよかったのかもしれない。。
            // 評価部分だけ実装するのはアリかも?
            val dartsByteArray = byteReader.copy(dsize)
            check(byteReader.offset == 10 * 4 + 32 + dsize.toInt())

            // tsize 分の token data
            // tsize はバイト単位。
            val tokens =
                (0 until tsize.toInt() / Token.SIZE)
                    .map { _ ->
                        Token(
                            byteReader.readUShort(),
                            byteReader.readUShort(),
                            byteReader.readUShort(),
                            byteReader.readShort(),
                            byteReader.readUInt(),
                            byteReader.readUInt(),
                        )
                    }.toList()

            // fsize 分の f data
            val features = byteReader.readRemaining()

            check(bytes.size == byteReader.offset)
            println(tokens.size)
            println(features.size)
            return Dict2(version, charset, Darts(dartsByteArray), tokens, features)
        }
    }
}

data class Dict2(
    val version: UInt,
    val charset: String,
    val darts: Darts,
    val tokens: List<Token>,
    private val features: ByteArray,
) {
    fun token(resultPair: Darts.ResultPair): Token = tokens[resultPair.value shr 8]

    fun feature(token: Token): String = extractStringFromByteArray(features, token.feature.toInt())

    private fun extractStringFromByteArray(
        byteArray: ByteArray,
        startIndex: Int,
    ): String {
        val endIndex = byteArray.drop(startIndex).indexOf(0)
        val validEndIndex = if (endIndex == -1) byteArray.size else endIndex + startIndex
        return byteArray.copyOfRange(startIndex, validEndIndex).decodeToString()
    }
}
