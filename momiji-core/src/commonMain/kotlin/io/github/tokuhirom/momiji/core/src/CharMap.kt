package io.github.tokuhirom.momiji.core.src

/**
 * - CATEGORY_NAME: Name of category. you have to define DEFAULT class.
 * - INVOKE: 1/0:   always invoke unknown word processing, evan when the word can be found in the lexicon
 * - GROUP:  1/0:   make a new word by grouping the same chracter category
 * - LENGTH: n:     1 to n length new words are added
 */
data class CharCategory(
    val name: String,
    val alwaysInvoke: Int,
    val grouping: Int,
    val length: Int? = null,
)

data class CodepointRange(
    val start: Int,
    val end: Int,
    val defaultCategory: String,
    val compatibleCategories: List<String> = listOf(),
)

class CharMap2(
    private val categories: List<String>,
    private val charInfos: List<CharInfo>,
) {
    fun resolve(char: Char): CharInfo {
        println(charInfos[char.code].defaultType)
        return charInfos[char.code]
    }

    fun categoryName(index: Int) = categories[index]
}

/**
 * CharMap is a map of character categories and codepoint ranges.
 */
class CharMap(
    charCategories: List<CharCategory>,
    private val codepoint2category: Array<CodepointRange?>,
) {
    private val categories: Map<String, CharCategory> =
        charCategories.associateBy {
            it.name
        }

    // TODO: This implementation doesn't care about the aliases.
    fun resolve(char: Char): CharCategory? =
        codepoint2category[char.code]?.let {
            categories[it.defaultCategory]!!
        }

    override fun toString(): String = "CharMap(categories=$categories, codepoint2category=$codepoint2category   )"

    companion object {
        /**
         * Parse a text representation of a character map.
         * It's the char.def in mecab's dictionary.
         *f
         * @param src The text representation of the character map.
         */
        fun parseText(src: String): CharMap {
            val categories = mutableListOf<CharCategory>()
            val codepoints = mutableListOf<CodepointRange>()

            src.split("\n").forEach { line ->
                val trimmedLine = line.trim().replace("#.*".toRegex(), "")

                // コメント行や空行をスキップ
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) return@forEach

                // カテゴリ定義のパース
                val categoryMatch = Regex("""^([A-Z]+)\s+(\d)\s+(\d)\s+(\d+)$""").matchEntire(trimmedLine)
                if (categoryMatch != null) {
                    val (name, timing, grouping, length) = categoryMatch.destructured
                    categories.add(
                        CharCategory(
                            name,
                            timing.toInt(),
                            grouping.toInt(),
                            length.toInt().let {
                                when (it) {
                                    0 -> null
                                    else -> length.toInt()
                                }
                            },
                        ),
                    )
                    return@forEach
                }

                // コードポイント定義のパース
                val codepointMatch = Regex("""^0x([0-9A-Fa-f]+)(?:\.\.0x([0-9A-Fa-f]+))?\s+([A-Z]+)(.*)$""").matchEntire(trimmedLine)
                if (codepointMatch != null) {
                    val (start, end, defaultCategory, compatibleCategories) = codepointMatch.destructured
                    val startInt = start.toInt(16)
                    val endInt = if (end.isEmpty()) startInt else end.toInt(16)
                    val compatibleList = compatibleCategories.trim().split("\\s+").filter { it.isNotEmpty() }
                    codepoints.add(CodepointRange(startInt, endInt, defaultCategory, compatibleList))
                }
            }

            val codepoint2category = buildArray(codepoints)
            return CharMap(categories, codepoint2category)
        }

        /**
         * Parse the binary format of the char.bin file.
         *
         * @see <a href="https://github.com/taku910/mecab/blob/master/mecab/src/char_property.cpp">char_property.cpp</a>
         */
        fun parseBinary(byteArray: ByteArray): CharMap2 {
            // get the size of the categories.
            // it's `unsigned int`. it means unsigned 32 bit.
            val size =
                (
                    byteArray[0].toUInt() +
                        (byteArray[1].toUInt() shl 8) +
                        (byteArray[2].toUInt() shl 16) +
                        (byteArray[3].toUInt() shl 24)
                ).toInt()

            val categories =
                (0 until size).map { i ->
                    val buf = byteArray.copyOfRange(4 + 32 * i, 4 + 32 * (i + 1))
                    // buf の \0 までの間を String として取り出す
                    val p: ByteArray = buf.filter { it != 0.toByte() }.toByteArray()
                    p.decodeToString()
                }

            val offset = 4 + 32 * size
            val charInfos =
                (0 until 0xFFFF - 1)
                    .map {
                        CharInfo.fromByteArray(byteArray, offset + it * 4)
                    }.toList()
            return CharMap2(categories, charInfos)
        }

        private fun buildArray(codepointRanges: List<CodepointRange>): Array<CodepointRange?> {
            val categoryArray: Array<CodepointRange?> = arrayOfNulls(65536)
            codepointRanges.forEach { range ->
                for (codepoint in range.start..range.end) {
                    categoryArray[codepoint] = range
                }
            }
            return categoryArray
        }
    }
}

data class CharInfo(
    val type: Int,
    val defaultType: Int,
    val length: Int,
    val group: Boolean,
    val invoke: Boolean,
) {
    companion object {
        fun fromByteArray(
            byteArray: ByteArray,
            offset: Int,
        ): CharInfo {
            // 18 bits = 8 + 8 + 2
            val type = type(byteArray, offset)
            // 8 bits = 6 + 2
            val defaultType = defaultType(byteArray, offset)
            val length = length(byteArray, offset)
            val group = group(byteArray, offset)
            val invoke = invoke(byteArray, offset)
            return CharInfo(type, defaultType, length, group, invoke)
        }

        private fun type(
            byteArray: ByteArray,
            offset: Int,
        ): Int =
            (
                byteArray[0 + offset].toUInt() or
                    (byteArray[1 + offset].toUInt() shl 8) or
                    ((byteArray[2 + offset].toUInt() and 0x03u) shl 16)
            ).toInt()

        private fun defaultType(
            byteArray: ByteArray,
            offset: Int,
        ): Int =
            (
                (byteArray[2 + offset].toUInt() shr 2) or
                    ((byteArray[3 + offset].toUInt() and 0x03u) shl 6)
            ).toInt()

        private fun length(
            byteArray: ByteArray,
            offset: Int,
        ): Int =
            (
                (byteArray[3 + offset].toUInt() shr 2) and 0x0Fu
            ).toInt()

        private fun group(
            byteArray: ByteArray,
            offset: Int,
        ): Boolean =
            (
                (byteArray[3 + offset].toUInt() shr 6) and 0x01u
            ) != 0u

        private fun invoke(
            byteArray: ByteArray,
            offset: Int,
        ) = (
            (byteArray[3 + offset].toUInt() shr 7) and 0x01u
        ) != 0u
    }
}
