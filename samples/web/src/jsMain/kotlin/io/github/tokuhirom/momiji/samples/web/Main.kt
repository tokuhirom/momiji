package io.github.tokuhirom.momiji.samples.web

import io.github.tokuhirom.momiji.core.CostManager
import io.github.tokuhirom.momiji.core.LatticeBuilder
import io.github.tokuhirom.momiji.core.character.CharMap
import io.github.tokuhirom.momiji.core.dict.Dict
import io.github.tokuhirom.momiji.core.matrix.Matrix
import io.github.tokuhirom.momiji.core.unknown.DefaultUnknownWordDetector
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class WebApp {
    private var latticeBuilder: Deferred<LatticeBuilder> =
        GlobalScope.async {
            loadLatticeBuilder()
        }
    private val resultElement = document.getElementById("result")!!
    private val loadingStatusElement = document.getElementById("loadingStatus")!!
    private val httpClient =
        HttpClient(Js) {
            install(Logging) {
                level = LogLevel.INFO
            }
        }

    fun start(input: String) {
        GlobalScope.launch {
            console.log("Loading dictionaries...")
            resultElement.textContent = "Loading dictionaries..."
            val builder = latticeBuilder.await()
            console.log("Start analyzing...")
            analyze(input, builder)
        }
    }

    private fun analyze(
        input: String,
        latticeBuilder: LatticeBuilder,
    ) {
        console.log("Building lattice...")
        resultElement.textContent = "Building lattice..."
        val lattice = latticeBuilder.buildLattice(input)
        console.log("Analyzing lattice by viterbi algorithm...")
        resultElement.textContent = "Analyzing lattice by viterbi algorithm..."
        val nodes = lattice.viterbi()
        val result = nodes.joinToString("\n") { it.toString() }
        resultElement.textContent = result
    }

    private suspend fun <T> loadBinary(
        fileName: String,
        callback: suspend (ByteArray) -> T,
    ): T {
        val pathName = window.location.pathname + fileName

        console.log("Loading $pathName")
        loadingStatusElement.textContent = "Loading $pathName"

        val res = httpClient.get(pathName)
        val bytes = res.readBytes()
        val result: T = callback(bytes)
        console.log("Loaded $pathName")

        loadingStatusElement.textContent = "Loaded $pathName"

        return result
    }

    private suspend fun loadLatticeBuilder(): LatticeBuilder {
        val charMap =
            loadBinary("mecab-ipadic/char.bin") { bytes ->
                CharMap.parseBinary(bytes)
            }
        val matrix =
            loadBinary("mecab-ipadic/matrix.bin") { bytes ->
                Matrix.parseBinary(bytes)
            }
        val unknown =
            loadBinary("mecab-ipadic/unk.dic") { bytes ->
                Dict.parseBinary(bytes)
            }
        val sys =
            loadBinary("mecab-ipadic/sys.dic") { bytes ->
                Dict.parseBinary(bytes)
            }

        loadingStatusElement.textContent = "Loaded all dictionary data."

        val costManager = CostManager(matrix)
        val unknownWordDetector = DefaultUnknownWordDetector(charMap, unknown)
        return LatticeBuilder(sys, costManager, unknownWordDetector)
    }
}

fun main() {
    console.log("Hello Console World!")

    val inputElement = document.getElementById("in")!!
    val formElement = document.getElementById("myForm")!!

    val webApp = WebApp()

    formElement.addEventListener("submit", { event ->
        event.preventDefault()
        event.stopPropagation()

        val input = inputElement.asDynamic().value as String

        webApp.start(input)
    })
}
