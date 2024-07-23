package io.github.tokuhirom.momiji.ipadic

import kotlin.test.Test
import kotlin.test.assertEquals

class MomijiIpadicLoaderTest {
    @Test
    fun testLoad() {
        val loader = MomijiIpadicLoader()
        loader.load()
    }

    @Test
    fun testLoadMatrix() {
        val loader = MomijiIpadicLoader()
        val matrix = loader.loadMatrix()
        assertEquals(1316, matrix.lsize)
        assertEquals(1316, matrix.rsize)

        assertEquals(-129, matrix.find(1315, 1315))
        assertEquals(-434, matrix.find(0, 0))
    }
}
