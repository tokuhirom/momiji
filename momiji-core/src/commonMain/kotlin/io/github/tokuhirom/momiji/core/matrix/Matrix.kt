package io.github.tokuhirom.momiji.core.matrix

/**
 * 連接コストのマトリクス
 */
data class Matrix(
    // Map<Pair<leftContextId, rightContextId>, cost>
    val lsize: Int,
    val rsize: Int,
    private val matrix: List<Short>,
) {
    fun find(
        leftContextId: Int,
        rightContextId: Int,
    ): Short = matrix[leftContextId + lsize * rightContextId]

    companion object {
        /**
         * Parse Mecab's matrix.bin format
         * @see <a href="https://github.com/taku910/mecab/blob/master/mecab/src/connector.cpp">mecab/src/connector.cpp</a>
         */
        fun parseBinary(src: ByteArray): Matrix {
            // little endian
            val lsize = src[0].toInt() + (src[1].toInt() shl 8)
            val rsize = src[2].toInt() + (src[3].toInt() shl 8)
            val matrix =
                src.drop(4).chunked(2).map {
                    (it[0].toInt() + (it[1].toInt() shl 8)).toShort()
                }
            return Matrix(lsize, rsize, matrix)
        }

        /**
         * Parse Mecab's matrix.def format
         */
        fun parseText(src: String): Matrix {
            // 最初の行に連接表のサイズ(前件サイズ, 後件サイズ)を書きます. その後は, 連接表の前件の文脈 ID, 後件の文脈IDと, それに対応するコストを書きます.

            val lines = src.split("\n")
            val header = lines[0]
            val (lsize, rsize) = header.split(" ").map { it.toInt() }
            val matrix: Array<Short> = Array(lsize * rsize) { 0 }

            src
                .split("\n")
                .drop(1)
                .filter {
                    it.isNotBlank()
                }.forEach { line ->
                    val (l, r, c) = header.split(" ").map { line.toInt() }
                    matrix[l + lsize * r] = c.toShort()
                }

            return Matrix(lsize, rsize, matrix.toList())
        }
    }
}
