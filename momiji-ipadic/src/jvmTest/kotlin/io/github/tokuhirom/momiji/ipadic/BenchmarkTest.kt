package io.github.tokuhirom.momiji.ipadic

import kotlin.test.Test

class BenchmarkTest {
    private val loader = MomijiIpadicLoader()
    private val builder = loader.load()

    @Test
    fun testDot() {
        val t1 = System.currentTimeMillis()

        for (i in 0 until 10) {
            val lattice = builder.buildLattice("結果的に、デバッガで追わないとよくわからんということになりがち。")
            lattice.viterbi()
        }

        val t2 = System.currentTimeMillis()

        println("Time: ${t2 - t1}ms")
    }
}
