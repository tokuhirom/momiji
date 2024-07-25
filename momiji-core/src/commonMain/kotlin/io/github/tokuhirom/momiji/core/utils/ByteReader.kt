package io.github.tokuhirom.momiji.core.utils

internal class ByteReader(
    private val bytes: ByteArray,
) {
    internal var offset = 0

    // read unsigned 32bit
    fun readUInt(): UInt {
        val result =
            (bytes[offset].toUInt() and 0xFFu) or
                ((bytes[offset + 1].toUInt() and 0xFFu) shl 8) or
                ((bytes[offset + 2].toUInt() and 0xFFu) shl 16) or
                ((bytes[offset + 3].toUInt() and 0xFFu) shl 24)

        offset += 4

        return result
    }

    // read signed 32bit
    fun readInt(): Int {
        val result =
            (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 3].toInt() and 0xFF) shl 24)

        offset += 4

        return result
    }

    // read unsigned 16bit
    fun readUShort(): UShort {
        val result =
            (bytes[offset].toUInt() and 0xFFu) or
                ((bytes[offset + 1].toUInt() and 0xFFu) shl 8)

        offset += 2

        return result.toUShort()
    }

    // read signed 16bit
    fun readShort(): Short {
        val result =
            (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8)

        offset += 2

        return result.toShort()
    }

    fun readNullFilledString(size: Int): String {
        val bytes = bytes.copyOfRange(offset, offset + size).filter { it != 0.toByte() }.toByteArray()
        offset += size
        return bytes.decodeToString()
    }

    fun copy(size: UInt): ByteArray {
        val bytes = bytes.copyOfRange(offset, offset + size.toInt())
        offset += size.toInt()
        return bytes
    }

    fun readRemaining(): ByteArray {
        val got = bytes.drop(offset)
        offset += got.size
        return got.toByteArray()
    }
}
