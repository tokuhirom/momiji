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

    @Test
    fun testLoadCharMap() {
        val loader = MomijiIpadicLoader()
        val charMap = loader.loadCharMap()
        assertEquals("KANJINUMERIC", charMap.resolve('一')?.name)
        assertEquals("KANJINUMERIC", charMap.resolve('億')?.name)
        assertEquals("KANJI", charMap.resolve('愛')?.name)
        assertEquals("SYMBOL", charMap.resolve('&')?.name)
    }

    @Test
    fun testMain() {
        val loader = MomijiIpadicLoader()
        val engine = loader.load()

        listOf(
            "東京都" to
                listOf(
                    "__BOS__ / null",
                    "東京 / 名詞,固有名詞,地域,一般,*,*,東京,トウキョウ,トーキョー",
                    "都 / 名詞,接尾,地域,*,*,*,都,ト,ト",
                    "__EOS__ / null",
                ),
            "自然言語" to
                listOf(
                    "__BOS__ / null",
                    "自然 / 名詞,形容動詞語幹,*,*,*,*,自然,シゼン,シゼン",
                    "言語 / 名詞,一般,*,*,*,*,言語,ゲンゴ,ゲンゴ",
                    "__EOS__ / null",
                ),
            "吾輩はネコである。" to
                listOf(
                    "__BOS__ / null",
                    "吾輩 / 名詞,代名詞,一般,*,*,*,吾輩,ワガハイ,ワガハイ",
                    "は / 助詞,係助詞,*,*,*,*,は,ハ,ワ",
                    "ネコ / 名詞,一般,*,*,*,*,ネコ,ネコ,ネコ",
                    "で / 助動詞,*,*,*,特殊・ダ,連用形,だ,デ,デ",
                    "ある / 助動詞,*,*,*,五段・ラ行アル,基本形,ある,アル,アル",
                    "。 / 記号,句点,*,*,*,*,。,。,。",
                    "__EOS__ / null",
                ),
            "Taiyaki" to
                listOf(
                    "__BOS__ / null",
                    "Taiyaki / 感動詞,*,*,*,*,*,*",
                    "__EOS__ / null",
                ),
            "Taiyakiは形態素解析エンジンである" to
                listOf(
                    "__BOS__ / null",
                    "Taiyaki / 名詞,一般,*,*,*,*,*",
                    "は / 助詞,係助詞,*,*,*,*,は,ハ,ワ",
                    "形態素 / 名詞,一般,*,*,*,*,形態素,ケイタイソ,ケイタイソ",
                    "解析 / 名詞,サ変接続,*,*,*,*,解析,カイセキ,カイセキ",
                    "エンジン / 名詞,一般,*,*,*,*,エンジン,エンジン,エンジン",
                    "で / 助動詞,*,*,*,特殊・ダ,連用形,だ,デ,デ",
                    "ある / 助動詞,*,*,*,五段・ラ行アル,基本形,ある,アル,アル",
                    "__EOS__ / null",
                ),
            "一億三千万円" to
                listOf(
                    "__BOS__ / null",
                    "一 / 名詞,数,*,*,*,*,一,イチ,イチ",
                    "億 / 名詞,数,*,*,*,*,億,オク,オク",
                    "三 / 名詞,数,*,*,*,*,三,サン,サン",
                    "千 / 名詞,数,*,*,*,*,千,セン,セン",
                    "万 / 名詞,数,*,*,*,*,万,マン,マン",
                    "円 / 名詞,接尾,助数詞,*,*,*,円,エン,エン",
                    "__EOS__ / null",
                ),
        ).forEach { (input, expected) ->
            println("# $input")
            val lattice = engine.buildLattice(input)
            val nodes = lattice.viterbi()

            nodes.forEach {
                println("  \"" + it.surface + " / " + it.dictRow?.annotations + "\",")
            }

            assertEquals(
                expected,
                nodes.map { it.surface + " / " + it.dictRow?.annotations },
            )
        }
    }

    @Test
    fun loadDict() {
        val loader = MomijiIpadicLoader()
        val dict = loader.loadDict()
        dict["東京"].forEach {
            assertEquals("東京", it.surface)
            assertEquals("名詞,固有名詞,地域,一般,*,*,東京,トウキョウ,トーキョー", it.annotations)
        }
    }

    @Test
    fun loadUnknown() {
        val loader = MomijiIpadicLoader()
        val unknown = loader.loadUnknown()
        assertEquals(7, unknown["HIRAGANA"].size)
    }
}
