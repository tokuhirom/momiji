package io.github.tokuhirom.momiji.core.dict

import io.github.tokuhirom.momiji.core.dict.Darts.ResultPair
import io.github.tokuhirom.momiji.core.utils.ByteReader

// TODO rename to Dict
data class Dict(
    val version: UInt,
    val charset: String,
    private val darts: Darts,
    val tokens: List<Token>,
    private val features: ByteArray,
) {
    fun commonPrefixSearch(key: ByteArray): List<DictNode> {
        val results = darts.commonPrefixSearch(key)
        return results.flatMap { result ->
            val surface =
                key
                    .copyOfRange(0, result.length)
                    .decodeToString()
            this.findTokens(result).map {
                DictNode(
                    surface,
                    it.token,
                    it.feature,
                )
            }
        }
    }

    private fun token(
        resultPair: ResultPair,
        index: Int,
    ): Token = tokens[(resultPair.value shr 8) + index]

    private fun feature(token: Token): String = extractStringFromByteArray(features, token.feature.toInt())

    private fun extractStringFromByteArray(
        byteArray: ByteArray,
        startIndex: Int,
    ): String {
        val endIndex = byteArray.drop(startIndex).indexOf(0)
        val validEndIndex = if (endIndex == -1) byteArray.size else endIndex + startIndex
        return byteArray.copyOfRange(startIndex, validEndIndex).decodeToString()
    }

    private fun tokenSize(resultPair: ResultPair): Int = resultPair.value and 0xFF

    fun findTokens(resultPair: ResultPair): List<TokenWithFeature> {
        val size = this.tokenSize(resultPair)
        return (0 until size).map {
            val token = this.token(resultPair, it)
            val feature = this.feature(token)
            TokenWithFeature(token, feature)
        }
    }

    data class TokenWithFeature(
        val token: Token,
        val feature: String,
    )

    companion object {
        private const val DICTIONARY_MAGIC_ID: UInt = 0xef718f77u

        fun parseBinary(bytes: ByteArray): Dict {
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
            return Dict(version, charset, Darts(dartsByteArray), tokens, features)
        }
    }
}
