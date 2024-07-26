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
        listOf(
            "CYRILLIC" to listOf("CYRILLIC"),
            "KANJIです" to listOf("KANJI"),
        ).forEach { (src, expected) ->
            val nodes = dict.commonPrefixSearch(src.toByteArray())
            assertEquals(expected, nodes.map { it.toString() })
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
            val word = "相生町"
            val nodes = dict.commonPrefixSearch(word.toByteArray())
            assertEquals(
                listOf(
                    "相生,1293,1293,8546,名詞,固有名詞,地域,一般,*,*,相生,アイオイ,アイオイ",
                    "相生,1285,1285,5660,名詞,一般,*,*,*,*,相生,アイオイ,アイオイ",
                    "相生,1288,1288,8497,名詞,固有名詞,一般,*,*,*,相生,アイオイ,アイオイ",
                    "相生,1290,1290,7579,名詞,固有名詞,人名,姓,*,*,相生,アイオイ,アイオイ",
                ),
                nodes.map { it.toString() },
            )
        }
    }
}
