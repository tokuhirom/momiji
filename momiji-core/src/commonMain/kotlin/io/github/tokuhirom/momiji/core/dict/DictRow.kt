package io.github.tokuhirom.momiji.core.dict

/**
 * DictRow represents a row in the mecab's dictionary.
 *
 * @param surface 表層形
 * @param leftId 左文脈ID
 * @param rightId 右文脈ID
 * @param cost 単語コスト
 * @param annotations その他のカラム
 */
data class DictRow(
    val surface: String, // 表層形
    val leftId: Int, // 左文脈ID
    val rightId: Int, // 右文脈ID
    val cost: Int, // 単語コスト
    val annotations: String, // その他のカラム
) {
    // return as a csv format
    override fun toString(): String =
        listOf(
            surface,
            leftId,
            rightId,
            cost,
            annotations,
        ).joinToString(",")

    companion object {
        /**
         * Parse a line in the dictionary.
         */
        fun parseLine(line: String): DictRow {
            val columns = line.split(",", limit = 5) // 最初の5個まで分割
            return DictRow(
                surface = columns[0],
                leftId = columns[1].toInt(),
                rightId = columns[2].toInt(),
                cost = columns[3].toInt(),
                annotations = columns[4],
            )
        }
    }
}
