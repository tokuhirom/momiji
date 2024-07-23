package io.github.tokuhirom.momiji.ipadic

import kotlin.test.Test

class MomijiIpadicLoaderTest {
    @Test
    fun testLoad() {
        val loader = MomijiIpadicLoader()
        loader.load()
    }
}
