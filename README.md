# momiji

Pure Kotlin の **形態素解析機** の実装です。
Pure Kotlin で実装されているため、**KMP**(Kotlin multiplatform) で利用可能です。

現在、以下のプラットフォームで CI を回していて、動いています。

- jvm
- js(nodejs)
- macosArm64
- macosX64
- linuxX64

MeCab のバイナリー辞書を利用可能です。

工藤 拓氏の [実践・自然言語処理シリーズ2　形態素解析の理論と実装](https://amzn.to/3Y9Ufo3) を参考に実装していて、基本的な挙動は MeCab と同じになるようにしています。

以下の artifact があります。

- momiji-core: 形態素解析エンジンのパッケージ
- momiji-ipadic: mecab-ipadic の辞書のパッケージ(kotlin コードに変換してバンドルしています)
- momiji-binary-dict: mecab のバイナリ辞書をロードする機能を提供します。

## 辞書

mecab-ipadic の辞書を利用することを推奨します。辞書のバンドルは momiji-ipadic artifact に含まれています。
gradle で依存にいれるだけで使えるので、一番カンタンに使えます。

UniDic は ファイルサイズが 1GB を超えるため、artifact としての配布はしていません。momiji-binary-dict を利用してロードしてください。

## How to build(for Momiji developers)

    ./gradlew buildDict build

## 参考文献

 * [Taiyaki](https://www.jonki.net/entry/2019/12/01/000807)
 * [kyotaw](https://kyotaw.hatenablog.jp/entry/2015/02/16/021417)
 * [sudachi.rs](https://qiita.com/sorami/items/7934fec2074c493c0f7d)
 * [MeCab](https://taku910.github.io/mecab/dic-detail.html)
