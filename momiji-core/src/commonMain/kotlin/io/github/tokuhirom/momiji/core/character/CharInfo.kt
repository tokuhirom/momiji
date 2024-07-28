package io.github.tokuhirom.momiji.core.character

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
