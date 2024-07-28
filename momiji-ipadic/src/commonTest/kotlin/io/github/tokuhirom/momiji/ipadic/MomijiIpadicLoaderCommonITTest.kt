package io.github.tokuhirom.momiji.ipadic

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class MomijiIpadicLoaderCommonITTest :
    StringSpec({
        val loader = MomijiIpadicLoader()
        val builder = loader.load()

        "should correctly parse input text" {
            forAll(
                row(
                    "東京都",
                    listOf(
                        "__BOS__ / null",
                        "東京 / 名詞,固有名詞,地域,一般,*,*,東京,トウキョウ,トーキョー",
                        "都 / 名詞,接尾,地域,*,*,*,都,ト,ト",
                        "__EOS__ / null",
                    ),
                ),
                row(
                    "自然言語",
                    listOf(
                        "__BOS__ / null",
                        "自然 / 名詞,形容動詞語幹,*,*,*,*,自然,シゼン,シゼン",
                        "言語 / 名詞,一般,*,*,*,*,言語,ゲンゴ,ゲンゴ",
                        "__EOS__ / null",
                    ),
                ),
                row(
                    "吾輩はネコである。",
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
                ),
                row(
                    "Taiyaki",
                    listOf(
                        "__BOS__ / null",
                        "Taiyaki / 感動詞,*,*,*,*,*,*",
                        "__EOS__ / null",
                    ),
                ),
                row(
                    "Taiyakiは形態素解析エンジンである",
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
                ),
                row(
                    "一億三千万円",
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
                ),
            ) { input, expected ->
                val lattice = builder.buildLattice(input)
                val nodes = lattice.viterbi()
                nodes.map { it.surface + " / " + it.dictRow?.feature } shouldBe expected
            }
        }
    })
