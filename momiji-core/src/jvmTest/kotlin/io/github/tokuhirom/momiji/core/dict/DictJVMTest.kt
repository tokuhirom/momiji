package io.github.tokuhirom.momiji.core.dict

import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class DictJVMTest {
    private val fileSystem = FileSystem.SYSTEM

    @Test
    fun testBinary() {
        val path = "../build/dict/unk.dic".toPath().resolve("momiji-ipadic/")
        if (!fileSystem.exists(path)) {
            println("file not found. skip the test.: $path")
            return
        }

        val byteArray =
            fileSystem.read(path) {
                this.readByteArray()
            }
        val dict = Dict.parseBinary(byteArray)

        // https://github.com/taku910/mecab/blob/05481e751dd5aa536a2bace46715ce54568b972a/mecab/configure.in#L10
        assertEquals(102u, dict.version)
        assertEquals("yes", dict.charset)
    }
}
