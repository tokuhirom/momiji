package io.github.tokuhirom.momiji.core.character

import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class CharMapJVMTest {
    private val fileSystem = FileSystem.SYSTEM

    @Test
    fun testBinary() {
        val path = "../".toPath().resolve("build/dict/char.bin")
        if (!fileSystem.exists(path)) {
            println("file not found. skip the test.: $path")
            return
        }

        val byteArray =
            fileSystem.read(path) {
                this.readByteArray()
            }
        val charmap = CharMap.parseBinary(byteArray)
        listOf(
            'A' to "ALPHA 1 1 0",
            '0' to "NUMERIC 1 1 0",
            'あ' to "HIRAGANA 0 1 2",
            'ア' to "KATAKANA 1 1 2",
            '漢' to "KANJI 0 0 2",
            ' ' to "SPACE 0 1 0",
            '億' to "KANJINUMERIC 1 1 0",
            'Ω' to "GREEK 1 1 0",
            'Ж' to "CYRILLIC 1 1 0",
            '^' to "SYMBOL 1 1 0",
        ).forEach { (ch, expected) ->
            val info = charmap.resolve(ch)
            println("$ch $info")
            assertEquals(
                expected,
                charmap.categoryName(info.defaultType) + " " +
                    (if (info.invoke) 1 else 0) + " " +
                    (if (info.group) 1 else 0) + " " +
                    info.length,
            )
        }
    }
}
