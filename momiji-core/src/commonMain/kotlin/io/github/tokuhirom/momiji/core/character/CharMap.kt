package io.github.tokuhirom.momiji.core.character

/**
 * CharMap is a map of character categories and codepoint ranges.
 */
data class CharMap(
    private val categories: List<String>,
    private val charInfos: List<CharInfo>,
) {
    fun resolve(char: Char): CharInfo = charInfos[char.code]

    fun categoryName(index: Int) = categories[index]

    companion object {
        /**
         * Parse a text representation of a character map.
         * It's the char.def in mecab's dictionary.
         *f
         * @param src The text representation of the character map.
         */
        fun parseText(src: String): CharMap = CharMapTextParser().parseText(src)

        /**
         * Parse the binary format of the char.bin file.
         *
         * @see <a href="https://github.com/taku910/mecab/blob/master/mecab/src/char_property.cpp">char_property.cpp</a>
         */
        fun parseBinary(byteArray: ByteArray): CharMap {
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
            return CharMap(categories, charInfos)
        }
    }
}
