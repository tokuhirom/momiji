package io.github.tokuhirom.momiji.core.matrix

/**
 * Transition cost matrix
 */
data class Matrix(
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
    }
}
