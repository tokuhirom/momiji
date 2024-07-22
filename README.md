# Momiji

Pure Kotlin で書かれた形態素解析エンジンです。
KMP(Kotlin multiplatform) に対応しています。

MeCab の辞書を使っています。

## Supported Environments

- Kotlin 2.0.0 or later
- One of the following environments:
  - JVM 17.0 or later
  - JS
  - Linux (x64)
  - macOS (Arm, x64)

## Installation

Add the following dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.tokuhirom.momiji:momiji:1.0.0")
}
```

## Usage

TBD

## License

The ported version of this library, written in Kotlin, is distributed under the MIT License as described below.

```
The MIT License (MIT)

Copyright © 2024 Tokuhiro Matsuno, http://64p.org/ <tokuhirom@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the “Software”), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO, THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```

This library is a port of the original [darts-clone](https://github.com/s-yata/darts-clone), which was originally written in C++. The original darts-clone is distributed under the BSD 2-clause license, as described below.

```

