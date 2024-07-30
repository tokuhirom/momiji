package io.github.tokuhirom.momiji.samples.web

import io.github.tokuhirom.momiji.core.CostManager
import io.github.tokuhirom.momiji.core.LatticeBuilder
import io.github.tokuhirom.momiji.core.Node
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
import mui.material.CssBaseline
import mui.material.Grid
import mui.material.GridProps
import mui.system.Breakpoint.Companion.xs
import mui.system.responsive
import react.FC
import react.Fragment
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.textarea
import react.dom.html.ReactHTML.tr
import react.dom.html.ReactHTML.ul
import react.useEffect
import react.useEffectOnce
import react.useState
import web.dom.document

// https://github.com/karakum-team/kotlin-mui-showcase/blob/5b7263a6a1379e40297f335f9e6be07e161dc9a7/src/jsMain/kotlin/team/karakum/MissedWrappers.kt#L8
inline var GridProps.xs: Int
    get() = TODO("Prop is write-only!")
    set(value) {
        asDynamic().xs = value
    }

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

private fun loadLatticeBuilder(
    charMap: CharMap,
    matrix: Matrix,
    unknown: Dict,
    sys: Dict,
): LatticeBuilder {
    val costManager = CostManager(matrix)
    val unknownWordDetector = DefaultUnknownWordDetector(charMap, unknown)
    return LatticeBuilder(sys, costManager, unknownWordDetector)
}

private val scope = MainScope()

val MyContent =
    FC {
        val (latticeBuilder, setLatticeBuilder) = useState<LatticeBuilder?>(null)
        val (input, setInput) = useState("日本語のサンプル文言でございます。")
        val (result, setResult) = useState(emptyList<Node>())
        val (charMap, setCharMap) = useState<CharMap?>(null)
        val (matrix, setMatrix) = useState<Matrix?>(null)
        val (unknown, setUnknown) = useState<Dict?>(null)
        val (sys, setSys) = useState<Dict?>(null)

        div {
            h1 {
                if (latticeBuilder != null) {
                    +"Momiji: Ready to use."
                } else {
                    +"Momiji: Loading dictionary..."
                }
            }

            Grid {
                container = true
                spacing = responsive(2)

                Grid {
                    item = true
                    xs = 4

                    p { +"日本語形態素解析機 Momiji のデモサイトです。" }

                    form {
                        useEffectOnce {
                            setCharMap(
                                loadBinary("mecab-ipadic/char.bin") { bytes ->
                                    CharMap.parseBinary(bytes)
                                },
                            )
                        }
                        useEffectOnce {
                            setMatrix(
                                loadBinary("mecab-ipadic/matrix.bin") { bytes ->
                                    Matrix.parseBinary(bytes)
                                },
                            )
                        }
                        useEffectOnce {
                            setUnknown(
                                loadBinary("mecab-ipadic/unk.dic") { bytes ->
                                    Dict.parseBinary(bytes)
                                },
                            )
                        }
                        useEffectOnce {
                            setSys(
                                loadBinary("mecab-ipadic/sys.dic") { bytes ->
                                    Dict.parseBinary(bytes)
                                },
                            )
                        }
                        useEffect(listOf(charMap, matrix, unknown, sys)) {
                            if (charMap != null && matrix != null && unknown != null && sys != null) {
                                setLatticeBuilder(loadLatticeBuilder(charMap, matrix, unknown, sys))
                            }
                        }

                        onSubmit = {
                            it.preventDefault()

                            scope.launch {
                                val latticeBuilder = loadLatticeBuilder(charMap!!, matrix!!, unknown!!, sys!!)
                                val lattice = latticeBuilder.buildLattice(input)
                                val nodes = lattice.viterbi()
                                setResult(nodes)
                            }
                        }

                        textarea {
                            value = input
                            cols = 40
                            onChange = { event -> setInput(event.target.value) }
                        }
                        button {
                            disabled = latticeBuilder == null
                            +"解析"
                        }
                    }

                    div {
                        table {
                            result.forEach { node ->
                                if (node is Node.BOS || node is Node.EOS) {
                                    return@forEach
                                }

                                tr {
                                    td {
                                        +node.surface
                                    }
                                    td {
                                        +(node.dictRow?.toString() ?: "")
                                    }
                                }
                            }
                        }
                    }
                }
                Grid {
                    item = true
                    xs = 4

                    ul {
                        li {
                            if (charMap != null) {
                                +"CharMap: Ready to use."
                            } else {
                                +"CharMap: Loading dictionary..."
                            }
                        }
                        li {
                            if (matrix != null) {
                                +"Matrix: Ready to use."
                            } else {
                                +"Matrix: Loading dictionary..."
                            }
                        }
                        li {
                            if (unknown != null) {
                                +"Unknown: Ready to use."
                            } else {
                                +"Unknown: Loading dictionary..."
                            }
                        }
                        li {
                            if (sys != null) {
                                +"Sys: Ready to use."
                            } else {
                                +"Sys: Loading dictionary..."
                            }
                        }
                        li {
                            if (latticeBuilder != null) {
                                +"LatticeBuilder: Ready to use."
                            } else {
                                +"LatticeBuilder: Loading dictionary..."
                            }
                        }
                    }
                }
            }
        }
    }

fun main() {
    console.log("Hello Console World!")

    val container = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(container).render(
        Fragment.create {
            CssBaseline()

            MyContent()
        },
    )
}
