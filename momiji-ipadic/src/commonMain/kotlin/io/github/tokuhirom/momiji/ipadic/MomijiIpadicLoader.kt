package io.github.tokuhirom.momiji.ipadic

import io.github.tokuhirom.kdary.KDary
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class MomijiIpadicLoader {
    @OptIn(ExperimentalEncodingApi::class)
    fun load() {
        println("Loading IPADIC...")
        val bytes = Base64.decode(KDARY_BASE64)
        val kdary = KDary.fromByteArray(bytes)
        println("Loaded chars: ${CHAR.length}")
        println("Loaded chars: ${DICT_CSV.length}")
    }
}
