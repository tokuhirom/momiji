package io.github.tokuhirom.momiji.core.dict

import io.github.tokuhirom.momiji.core.utils.ByteReader

/**
 * Darts is a Double-ARray Trie System.
 *
 * <p>
 *     This implementation only supports the read-only mode.
 *     And only supports the common prefix search.
 * </p>
 *
 * @see <a href="http://chasen.org/~taku/software/darts/">Darts</a>
 */
class Darts(
    byteArray: ByteArray,
) {
    private val array = buildUnits(byteArray)

    fun commonPrefixSearch(key: ByteArray): List<ResultPair> {
        var b = array[0].base
        var n = 0
        var p = 0

        val results = mutableListOf<ResultPair>()

        for (i in key.indices) {
            p = b
            n = array[p].base
            println("  $i $p $n")
            if (b.toUInt() == array[p].check && n < 0) {
                val value = -n - 1
                results.add(
                    ResultPair(
                        value,
                        i,
                    ),
                )
            }

            p = b + key[i].toUByte().toInt() + 1
            check(p >= 0) {
                "p: $p, b: $b, key[i]: ${key[i]}"
            }
            if (b.toUInt() == array[p].check) {
                b = array[p].base
            } else {
                return results
            }
        }

        p = b
        n = array[p].base
        if (b.toUInt() == array[p].check && n < 0) {
            val value = -n - 1
            results.add(
                ResultPair(
                    value,
                    key.size,
                ),
            )
        }
        return results
    }

    private fun buildUnits(byteArray: ByteArray): List<Unit> {
        val reader = ByteReader(byteArray)
        return (0 until byteArray.size / 8)
            .map {
                val base = reader.readInt()
                val check = reader.readUInt()
                Unit(base, check)
            }.toList()
    }

    // 64bit
    data class Unit(
        val base: Int,
        val check: UInt,
    )

    data class ResultPair(
        val value: Int,
        val length: Int,
    )
}
