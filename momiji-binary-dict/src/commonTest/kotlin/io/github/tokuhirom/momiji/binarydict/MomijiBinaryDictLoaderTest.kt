package io.github.tokuhirom.momiji.binarydict

import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class MomijiBinaryDictLoaderTest {
    private val fileSystem = getFileSystem()

    @Test
    fun testLoad() {
        val path = "build/dict"
        if (!fileSystem.exists(path.toPath())) {
            println("file not found. skip the test.: $path")
            return
        }

        val momiji = MomijiBinaryDictLoader(path).load()
        val lattice = momiji.buildLattice("村長さん")
        val nodes = lattice.viterbi()
        nodes.forEach {
            println(it)
        }
        assertEquals(4, nodes.size)
    }
}
