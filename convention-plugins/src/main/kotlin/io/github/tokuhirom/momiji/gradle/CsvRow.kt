package io.github.tokuhirom.momiji.gradle

data class CsvRow(
    val surface: String, // 表層形
    val raw: String,
) {
    override fun toString(): String = raw

    companion object {
        fun parse(raw: String): CsvRow {
            val columns = raw.split(",")
            return CsvRow(
                surface = columns[0],
                raw = raw,
            )
        }
    }
}
