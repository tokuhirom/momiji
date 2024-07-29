package io.github.tokuhirom.momiji.core.dict

import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class DartsJVMTest {
    private val fileSystem = FileSystem.SYSTEM

    @Test
    fun testFoo() {
        val path = "../build/dict/unk.dic".toPath()
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
            "CYRILLIC" to
                setOf(
                    "CYRILLIC,1285,1285,7966,名詞,一般,*,*,*,*,*",
                    "CYRILLIC,1293,1293,12600,名詞,固有名詞,地域,一般,*,*,*",
                    "CYRILLIC,1292,1292,8492,名詞,固有名詞,組織,*,*,*,*",
                    "CYRILLIC,1289,1289,12615,名詞,固有名詞,人名,一般,*,*,*",
                    "CYRILLIC,1288,1288,9866,名詞,固有名詞,一般,*,*,*,*",
                ),
            "KANJIです" to
                setOf(
                    "KANJI,1285,1285,11426,名詞,一般,*,*,*,*,*",
                    "KANJI,1283,1283,17290,名詞,サ変接続,*,*,*,*,*",
                    "KANJI,1293,1293,17611,名詞,固有名詞,地域,一般,*,*,*",
                    "KANJI,1292,1292,12649,名詞,固有名詞,組織,*,*,*,*",
                    "KANJI,1289,1289,17340,名詞,固有名詞,人名,一般,*,*,*",
                    "KANJI,1288,1288,15295,名詞,固有名詞,一般,*,*,*,*",
                ),
        ).forEach { (src, expected) ->
            val nodes = dict.commonPrefixSearch(src.toByteArray())
            assertEquals(expected, nodes.map { it.toString() }.toSet())
        }
    }

    @Test
    fun testSysDic() {
        val path = "../build/dict/sys.dic".toPath()
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
                setOf(
                    "相,1285,1285,10218,名詞,一般,*,*,*,*,相,ソウ,ソー",
                    "相,1285,1285,11060,名詞,一般,*,*,*,*,相,ショウ,ショー",
                    "相,1298,1298,9745,名詞,接尾,一般,*,*,*,相,ショウ,ショー",
                    "相,559,559,7567,接頭詞,動詞接続,*,*,*,*,相,アイ,アイ",
                    "相生,1293,1293,8546,名詞,固有名詞,地域,一般,*,*,相生,アイオイ,アイオイ",
                    "相生,1285,1285,5660,名詞,一般,*,*,*,*,相生,アイオイ,アイオイ",
                    "相生,1288,1288,8497,名詞,固有名詞,一般,*,*,*,相生,アイオイ,アイオイ",
                    "相生,1290,1290,7579,名詞,固有名詞,人名,姓,*,*,相生,アイオイ,アイオイ",
                ),
                nodes.map { it.toString() }.toSet(),
            )
        }
    }
}
