package io.github.tokuhirom.momiji.ipadic

import kotlin.test.Test
import kotlin.test.assertEquals

class MomijiIpadicLoaderCommonTest {
    @Test
    fun testLoadMatrix() {
        val loader = MomijiIpadicLoader()
        val matrix = loader.loadMatrix()
        assertEquals(1316, matrix.lsize)
        assertEquals(1316, matrix.rsize)

        assertEquals(-129, matrix.find(1315, 1315))
        assertEquals(-434, matrix.find(0, 0))
    }

    @Test
    fun testLoadCharMap() {
        val loader = MomijiIpadicLoader()
        val charMap = loader.loadCharMap()
        assertEquals("KANJINUMERIC", charMap.categoryName(charMap.resolve('一').defaultType))
        assertEquals("KANJINUMERIC", charMap.categoryName(charMap.resolve('億').defaultType))
        assertEquals("KANJI", charMap.categoryName(charMap.resolve('愛').defaultType))
        assertEquals("SYMBOL", charMap.categoryName(charMap.resolve('&').defaultType))
    }

    @Test
    fun loadDict() {
        val loader = MomijiIpadicLoader()
        val dict = loader.loadSysDic()
        val nodes = dict.commonPrefixSearch("東京".encodeToByteArray())
        println(nodes.joinToString("\n") { it.toString() })

        assertEquals(
            listOf(
                "東,1293,1293,11611,名詞,固有名詞,地域,一般,*,*,東,ヒガシ,ヒガシ",
                "東,1293,1293,12705,名詞,固有名詞,地域,一般,*,*,東,アズマ,アズマ",
                "東,1285,1285,6245,名詞,一般,*,*,*,*,東,ヒガシ,ヒガシ",
                "東,1285,1285,9781,名詞,一般,*,*,*,*,東,アズマ,アズマ",
                "東,1288,1288,11632,名詞,固有名詞,一般,*,*,*,東,ヒガシ,ヒガシ",
                "東,1290,1290,12256,名詞,固有名詞,人名,姓,*,*,東,アズマ,アズマ",
                "東,1290,1290,13358,名詞,固有名詞,人名,姓,*,*,東,ヒガシ,ヒガシ",
                "東,1291,1291,13228,名詞,固有名詞,人名,名,*,*,東,ヒガシ,ヒガシ",
                "東京,1293,1293,3003,名詞,固有名詞,地域,一般,*,*,東京,トウキョウ,トーキョー",
            ),
            nodes.map { it.toString() },
        )
    }

    @Test
    fun testLoadUnknown() {
        val loader = MomijiIpadicLoader()
        val unknown = loader.loadUnknown()
        val results = unknown.commonPrefixSearch("東京".encodeToByteArray())
        assertEquals(0, results.size)
    }
}
