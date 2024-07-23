package io.github.tokuhirom.momiji.engine.src.matrix

/**
 * 連接コストのマトリクス
 */
data class Matrix(
    // Map<Pair<leftContextId, rightContextId>, cost>
    private val connectionMap: Map<Pair<Int, Int>, Short>,
) {
    fun find(
        leftContextId: Int,
        rightContextId: Int,
        default: Short,
    ): Short = connectionMap.getOrDefault(leftContextId to rightContextId, default)

    companion object {
        fun parse(src: String): Matrix {
            // 最初の行に連接表のサイズ(前件サイズ, 後件サイズ)を書きます. その後は, 連接表の前件の文脈 ID, 後件の文脈IDと, それに対応するコストを書きます.
            // 最初の行は無視してよいです。
            return Matrix(
                src
                    .split("\n")
                    .drop(1)
                    .filter {
                        it.isNotBlank()
                    }.map {
                        Connection.parse(it)
                    }.associate {
                        (it.leftContextId to it.rightContextId) to it.cost
                    },
            )
        }
    }
}
