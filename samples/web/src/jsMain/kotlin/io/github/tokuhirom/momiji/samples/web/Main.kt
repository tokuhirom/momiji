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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WebApp {
    private var latticeBuilder: LatticeBuilder? = null
    private val resultElement = document.getElementById("result")!!

    fun start(input: String) {
        if (latticeBuilder == null) {
            console.log("Loading dictionaries...")
            resultElement.textContent = "Loading dictionaries..."
            GlobalScope.launch {
                latticeBuilder = loadLatticeBuilder()
                analyze(input)
            }
        } else {
            analyze(input)
        }
    }

    private fun analyze(input: String) {
        console.log("Building lattice...")
        resultElement.textContent = "Building lattice..."
        val lattice = latticeBuilder!!.buildLattice(input)
        console.log("Analyzing lattice by viterbi algorithm...")
        resultElement.textContent = "Analyzing lattice by viterbi algorithm..."
        val nodes = lattice.viterbi()
        val result = nodes.joinToString("\n") { it.toString() }
        resultElement.textContent = result
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

    document.getElementById("root")?.textContent = "Hello Kotlin World!"
}

suspend fun loadLatticeBuilder(): LatticeBuilder {
    val client =
        HttpClient(Js) {
            install(Logging) {
                level = LogLevel.INFO
            }
        }

    suspend fun <T> load(
        fileName: String,
        callback: suspend (ByteArray) -> T,
    ): T {
        console.log("Loading $fileName")
        val res = client.get("$fileName")
        val bytes = res.readBytes()
        val result: T = callback(bytes)
        console.log("Loaded $fileName")
        return result
    }

    val charMap =
        load("mecab-ipadic/char.bin") { bytes ->
            CharMap.parseBinary(bytes)
        }
    val matrix =
        load("mecab-ipadic/matrix.bin") { bytes ->
            Matrix.parseBinary(bytes)
        }
    val unknown =
        load("mecab-ipadic/unk.dic") { bytes ->
            Dict.parseBinary(bytes)
        }
    val sys =
        load("mecab-ipadic/sys.dic") { bytes ->
            Dict.parseBinary(bytes)
        }

    val costManager = CostManager(matrix)
    val unknownWordDetector = DefaultUnknownWordDetector(charMap, unknown)
    return LatticeBuilder(sys, costManager, unknownWordDetector)
}
