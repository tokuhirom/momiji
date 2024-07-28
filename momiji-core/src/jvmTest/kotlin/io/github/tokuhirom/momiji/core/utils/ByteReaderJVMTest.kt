package io.github.tokuhirom.momiji.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class ByteReaderJVMTest {
    @Test
    fun byteArrayTest() {
        // 4d 99 71 ef
        val byteArray = byteArrayOf(0x4d, 0x99.toByte(), 0x71, 0xef.toByte())
        val br = ByteReader(byteArray)
        assertEquals(4017199437u, br.readUInt())
    }
}
