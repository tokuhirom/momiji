# momiji

KDary を利用した形態素解析エンジンの実装です。
KMP(Kotlin multiplatform) 

MeCab の辞書を利用しています。工藤 拓氏の [実践・自然言語処理シリーズ2　形態素解析の理論と実装](https://amzn.to/3Y9Ufo3) を参考に実装していて、
基本的な挙動は MeCab と同じになるようにしています。

以下の artifact があります。

- momiji-engine: 形態素解析エンジンのパッケージ
- momiji-ipadic: ipadic の辞書のパッケージ(kotlin コードに変換してバンドルしています)

## 参考文献

 * [Taiyaki](https://www.jonki.net/entry/2019/12/01/000807)
 * [kyotaw](https://kyotaw.hatenablog.jp/entry/2015/02/16/021417)
 * [sudachi.rs](https://qiita.com/sorami/items/7934fec2074c493c0f7d)
 * [MeCab](https://taku910.github.io/mecab/dic-detail.html)
