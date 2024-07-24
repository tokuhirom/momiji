package io.github.tokuhirom.momiji.core.character

internal class CharMapTextParser {
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
            val trimmedLine = line.replace("#.*".toRegex(), "").trim()

            // コメント行や空行をスキップ
            if (trimmedLine.isEmpty()) return@forEach

            // カテゴリ定義のパース
            val categoryMatch = Regex("""^([A-Z]+)\s+(\d)\s+(\d)\s+(\d+)$""").matchEntire(trimmedLine)
            if (categoryMatch != null) {
                val (name, timing, grouping, length) = categoryMatch.destructured
                categories.add(
                    CharCategory(
                        name,
                        timing.toInt() == 1,
                        grouping.toInt() == 1,
                        length.toInt(),
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

        val categoryNames = categories.map { it.name }.toList()
        val codepoint2category =
            buildArray(
                categories,
                codepoints,
            )
        return CharMap(
            categoryNames,
            codepoint2category,
        )
    }

    private fun buildArray(
        categories: List<CharCategory>,
        codepointRanges: List<CodepointRange>,
    ): List<CharInfo> {
        val categoryMap =
            categories
                .mapIndexed { index, charCategory ->
                    charCategory.name to (charCategory to index)
                }.toMap()

        val defaultCategory = categoryMap["DEFAULT"]!!
        val defaultCharInfo = buildCharInfo(defaultCategory.second, defaultCategory.first)

        val categoryArray: Array<CharInfo?> = arrayOfNulls(65536)
        codepointRanges.forEach { range ->
            for (codepoint in range.start..range.end) {
                val category = categoryMap[range.defaultCategory]!!
                categoryArray[codepoint] = buildCharInfo(category.second, category.first)
            }
        }
        return categoryArray.map { it ?: defaultCharInfo }.toList()
    }

    private fun buildCharInfo(
        defaultType: Int,
        charCategory: CharCategory,
    ) = CharInfo(
        type = (1 shl defaultType),
        defaultType = defaultType,
        group = charCategory.grouping,
        invoke = charCategory.alwaysInvoke,
        length = charCategory.length,
    )

    /**
     * - CATEGORY_NAME: Name of category. you have to define DEFAULT class.
     * - INVOKE: 1/0:   always invoke unknown word processing, evan when the word can be found in the lexicon
     * - GROUP:  1/0:   make a new word by grouping the same chracter category
     * - LENGTH: n:     1 to n length new words are added
     */
    data class CharCategory(
        val name: String,
        val alwaysInvoke: Boolean,
        val grouping: Boolean,
        val length: Int,
    )

    data class CodepointRange(
        val start: Int,
        val end: Int,
        val defaultCategory: String,
        val compatibleCategories: List<String> = listOf(),
    )
}
