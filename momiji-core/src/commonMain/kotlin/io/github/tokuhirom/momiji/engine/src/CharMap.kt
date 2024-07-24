package io.github.tokuhirom.momiji.engine.src

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