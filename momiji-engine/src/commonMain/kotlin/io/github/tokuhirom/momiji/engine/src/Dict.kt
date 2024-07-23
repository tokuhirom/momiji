package io.github.tokuhirom.momiji.engine.src

import io.github.tokuhirom.momiji.engine.src.DictRow.Companion.parseLine

data class Dict(
    val data: Map<String, List<DictRow>>,
) {
    operator fun get(s: String): List<DictRow> = data.getOrDefault(s, emptyList())

    companion object {
        fun parse(src: String): Dict =
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
    }
}

data class DictRow(
    val surface: String, // 表層形
    val leftId: Int, // 左文脈ID
    val rightId: Int, // 右文脈ID
    val cost: Int, // 単語コスト
    val annotations: List<String>, // その他のカラム
) {
    // return as a csv format
    override fun toString(): String =
        listOf(
            surface,
            leftId,
            rightId,
            cost,
            annotations.joinToString(","),
        ).joinToString(",")

    companion object {
        fun parseLine(line: String): DictRow {
            val columns = line.split(",")
            return DictRow(
                surface = columns[0],
                leftId = columns[1].toInt(),
                rightId = columns[2].toInt(),
                cost = columns[3].toInt(),
                annotations = columns.drop(4),
            )
        }
    }
}
