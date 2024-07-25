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
//        const unsigned int DictionaryMagicID = 0xef718f77u;
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

        @OptIn(ExperimentalStdlibApi::class)
        fun parseBinary(bytes: ByteArray): Dict2 {
            val byteReader = ByteReader(bytes)

            /*
              // needs to be 64bit aligned
      // 10*32 = 64*5
      bofs.write(reinterpret_cast<const char *>(&magic),   sizeof(unsigned int));
      bofs.write(reinterpret_cast<const char *>(&version), sizeof(unsigned int));
      bofs.write(reinterpret_cast<const char *>(&type),    sizeof(unsigned int));
      bofs.write(reinterpret_cast<const char *>(&lexsize), sizeof(unsigned int));
      bofs.write(reinterpret_cast<const char *>(&lsize),   sizeof(unsigned int));
      bofs.write(reinterpret_cast<const char *>(&rsize),   sizeof(unsigned int));
      bofs.write(reinterpret_cast<const char *>(&dsize),   sizeof(unsigned int));
      bofs.write(reinterpret_cast<const char *>(&tsize),   sizeof(unsigned int));
      bofs.write(reinterpret_cast<const char *>(&fsize),   sizeof(unsigned int));
      bofs.write(reinterpret_cast<const char *>(&dummy),   sizeof(unsigned int));
             */
            println("readUInt: ${bytes.copyOfRange(0, 4).toHexString(HexFormat.Default)}")

            val magic = byteReader.readUInt()
            println("magic=$magic")
            val version = byteReader.readUInt()
            val type = byteReader.readUInt()
            val lexsize = byteReader.readUInt()
            val lsize = byteReader.readUInt()
            val rsize = byteReader.readUInt()
            val dsize = byteReader.readUInt()
            val tsize = byteReader.readUInt()
            val fsize = byteReader.readUInt()
            val dummy = byteReader.readUInt()

            println("version=$version type=$type lexsize=$lexsize lsize=$lsize rsize=$rsize dsize=$dsize tsize=$tsize fsize=$fsize")
            println(DICTIONARY_MAGIC_ID xor magic)
            println(bytes.size)
            println("magic=$magic")
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
            val features: List<String> =
                byteReader
                    .readRemaining()
                    .decodeToString()
                    .split(0x00.toChar())
                    .dropLast(1)
            println(features)

            check(bytes.size == byteReader.offset)
            println(tokens.size)
            println(features.size)
            return Dict2(version, charset, tokens, features)
        }
    }
}

data class Dict2(
    val version: UInt,
    val charset: String,
    val tokens: List<Token>,
    val features: List<String>,
)
