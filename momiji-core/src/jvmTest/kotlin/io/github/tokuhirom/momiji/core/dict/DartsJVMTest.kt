package io.github.tokuhirom.momiji.core.dict

import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class DartsJVMTest {
    private val fileSystem = FileSystem.SYSTEM

    @Test
    fun testFoo() {
        val path = "../".toPath().resolve("momiji-ipadic/build/dict/unk.dic")
        if (!fileSystem.exists(path)) {
            println("file not found. skip the test.: $path")
            return
        }

        val byteArray =
            fileSystem.read(path) {
                this.readByteArray()
            }
        val dict = Dict.parseBinary(byteArray)
        run {
            val results = dict.darts.commonPrefixSearch("CYRILLIC".toByteArray())
            assertEquals(1, results.size)
            assertEquals(8, results[0].length)
            assertEquals(1541, results[0].value)
        }
        run {
            val results = dict.darts.commonPrefixSearch("KANJIです".toByteArray())
            assertEquals(1, results.size)
            assertEquals(5, results[0].length)
            assertEquals(6150, results[0].value)
        }
    }

    @Test
    fun testSysDic() {
        val path = "../".toPath().resolve("momiji-ipadic/build/dict/sys.dic")
        if (!fileSystem.exists(path)) {
            println("file not found. skip the test.: $path")
            return
        }

        val byteArray =
            fileSystem.read(path) {
                this.readByteArray()
            }

        val dict = Dict.parseBinary(byteArray)
        run {
            val results = dict.darts.commonPrefixSearch("相生町".toByteArray())
            println(results)
            println(dict.tokens.size)
            assertEquals(2, results.size)

            assertEquals(3, results[0].length)
            assertEquals(76134660, results[0].value)
            assertEquals(4, results[0].tokenSize())
            val token0 = dict.token(results[0])
            assertEquals("名詞,一般,*,*,*,*,相,ソウ,ソー", dict.feature(token0))

            assertEquals(6, results[1].length)
            assertEquals(76239620, results[1].value)
            val token1 = dict.token(results[1])
            assertEquals("名詞,固有名詞,地域,一般,*,*,相生,アイオイ,アイオイ", dict.feature(token1))
        }
    }
}
