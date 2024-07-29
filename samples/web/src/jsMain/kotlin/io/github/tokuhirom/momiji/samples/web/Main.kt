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
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.FC
import react.Fragment
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.textarea
import react.useEffect
import react.useState
import web.dom.document

private val httpClient =
    HttpClient(Js) {
        install(Logging) {
            level = LogLevel.INFO
        }
    }

private suspend fun <T> loadBinary(
    fileName: String,
    callback: suspend (ByteArray) -> T,
): T {
    val pathName = window.location.pathname + fileName

    console.log("Loading $pathName")

    val res = httpClient.get(pathName)
    val bytes = res.readBytes()
    val result: T = callback(bytes)
    console.log("Loaded $pathName")

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

    val costManager = CostManager(matrix)
    val unknownWordDetector = DefaultUnknownWordDetector(charMap, unknown)
    return LatticeBuilder(sys, costManager, unknownWordDetector)
}

private val scope = MainScope()

val MyContent = FC {
    val (latticeBuilder, setLatticeBuilder) = useState<LatticeBuilder?>(null)
    val (input, setInput) = useState("日本語のサンプル文言でございます。")
    val (result, setResult) = useState<String>("")

    div {
        h1 {
            if (latticeBuilder != null) {
                +"Momiji: Ready to use."
            } else {
                +"Momiji: Loading dictionary..."
            }
        }

        p { +"日本語形態素解析機 Momiji のデモサイトです。" }

        form {
            useEffect {
                setLatticeBuilder(loadLatticeBuilder())
            }

            onSubmit = {
                it.preventDefault()

                scope.launch {
                    val latticeBuilder = loadLatticeBuilder()
                    val lattice = latticeBuilder.buildLattice(input)
                    val nodes = lattice.viterbi()
                    setResult(nodes.joinToString("\n") { node -> node.toString() })
                }
            }

            textarea {
                value = input
                onChange = { event -> setInput(event.target.value) }
            }
            button { +"解析" }
        }

        div {
            pre { +"$result" }
        }
    }
}


fun main() {
    console.log("Hello Console World!")

    val container = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(container).render(Fragment.create {
        MyContent()
    })
}
