import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.Charset
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

open class BuildDictTask : DefaultTask() {
    @Input
    var url = "https://drive.google.com/uc?export=download&id=0B4y35FiV1wh7MWVlSDBCSXZMTXM"

    @Input
    var dicType = "ipadic"

    @TaskAction
    fun run() {
        // mkdir -p build
        buildDir().mkdirs()

        val tarball = download()
        val mecabDictDir = extract(tarball)
        buildMecabDictionary(mecabDictDir)
        copyFiles(mecabDictDir)

        SourceCodeGenerator(project, dicType).generateAll(mecabDictDir)
    }

    class SourceCodeGenerator(
        private val project: Project,
        private val dicType: String,
    ) {
        fun generateAll(mecabDictDir: File) {
            writeBase64Chunks(
                src = mecabDictDir.resolve("sys.dic").readBytes(),
                filePrefix = "Sys",
                variablePrefix = "SYS",
            )

            writeBase64Chunks(
                src = mecabDictDir.resolve("unk.dic").readBytes(),
                filePrefix = "Unk",
                variablePrefix = "UNK",
            )

            writeBase64Chunks(
                src = mecabDictDir.resolve("char.bin").readBytes(),
                filePrefix = "Char",
                variablePrefix = "CHAR",
            )

            writeBase64Chunks(
                src = mecabDictDir.resolve("matrix.bin").readBytes(),
                filePrefix = "Matrix",
                variablePrefix = "Matrix",
            )
        }

        @OptIn(ExperimentalEncodingApi::class)
        private fun writeBase64Chunks(
            src: ByteArray,
            filePrefix: String,
            variablePrefix: String,
        ) {
            writeChunks(
                src = Base64.encode(src),
                filePrefix = filePrefix,
                variablePrefix = variablePrefix,
            )
        }

        private fun writeChunks(
            src: String,
            filePrefix: String,
            pkg: String = "io.github.tokuhirom.momiji.ipadic.${filePrefix.lowercase()}",
            variablePrefix: String,
        ) {
            val baseDir =
                project.layout.projectDirectory
                    .asFile
                    .resolve("src/generated/otherMain/kotlin/${pkg.replace(".", "/")}")
            baseDir.mkdirs()

            // JVM では文字列として 65535 文字が最大。
            // https://stackoverflow.com/questions/62098263/kotlin-string-max-length-kotlin-file-with-a-long-string-is-not-compiling
            val groups = splitStringByBytes(src)
            val chunkGroup = groups.chunked(10)

            chunkGroup.forEachIndexed { index, chunk ->
                val filename = baseDir.resolve("$filePrefix$index.kt")
                println("Writing $filename")
                filename.bufferedWriter().use { writer ->
                    writer.write("@file:Suppress(\"ktlint:standard:max-line-length\")\n\n")
                    writer.write("package $pkg\n\n")
                    writer.write("internal val ${variablePrefix}_$index = listOf(\n")
                    chunk.forEach {
                        writer.write("    \"\"\"${escapeKotlinString(it)}\"\"\",\n")
                    }
                    writer.write(").joinToString(\"\")\n")
                    writer.newLine()
                }
            }

            val filename = baseDir.resolve("$filePrefix.kt")
            println("Writing $filename")
            filename.bufferedWriter().use { writer ->
                writer.write("package $pkg\n\n")
                writer.write("val $variablePrefix = ")
                writer.write(
                    List(chunkGroup.size) { index ->
                        "${variablePrefix}_$index"
                    }.joinToString("+"),
                )
                writer.write("\n\n")
            }
        }

        private fun File.readEucJPText(): String =
            this.bufferedReader(Charset.forName("EUC-JP")).use { reader ->
                reader.readText()
            }

        private fun escapeKotlinString(src: String): String =
            src
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\$", "\${'\$'}")
                .replace("\r", "\\r")

        private fun splitStringByBytes(
            input: String,
            maxBytes: Int = 65535,
        ): List<String> {
            val charset = Charset.forName("UTF-8")
            val byteBuffer = input.toByteArray(charset)

            var start = 0
            val parts = mutableListOf<String>()

            while (start < byteBuffer.size) {
                var end = start + maxBytes
                if (end >= byteBuffer.size) {
                    end = byteBuffer.size
                } else {
                    // UTF-8のマルチバイト文字が途中で切れないように調整
                    while (end > start && (byteBuffer[end].toInt() and 0xC0) == 0x80) {
                        end--
                    }
                }

                val part = String(byteBuffer, start, end - start, charset)
                parts.add(part)
                start = end
            }

            return parts
        }
    }

    private fun buildDir() =
        project.layout.buildDirectory
            .get()
            .asFile

    private fun download(): File =
        runBlocking {
            val client =
                HttpClient {
                    install(HttpTimeout) {
                        requestTimeoutMillis = 3 * 60 * 1000
                        connectTimeoutMillis = 3 * 60 * 1000
                        socketTimeoutMillis = 3 * 60 * 1000
                    }
                }
            val response = client.get(url)
            val file = buildDir().resolve("mecab-$dicType.tar.gz")
            response.bodyAsChannel().copyTo(file.outputStream())
            println("Downloaded $file")
            file
        }

    private fun extract(tarball: File): File {
        val dictDir = buildDir().resolve("dict")
        dictDir.mkdirs()

        val processBuilder =
            ProcessBuilder(
                "tar",
                "-xzvf",
                tarball.absolutePath,
                "--strip-components=1",
                "-C",
                dictDir.absolutePath,
            ).redirectErrorStream(true)
        val process = processBuilder.start()
        process.waitFor()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        println("Extracted to $dictDir: $output")
        return dictDir
    }

    private fun buildMecabDictionary(dictDir: File) {
        listOf(
            listOf("./configure", "--with-charset", "utf-8"),
            listOf("make"),
        ).forEach { command ->
            val processBuilder =
                ProcessBuilder(command)
                    .directory(dictDir)
                    .redirectErrorStream(true)
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val status = process.waitFor()
            println("$command: $status: $output")
        }
    }

    private fun copyFiles(mecabDictDir: File) {
        val destDir =
            project.layout.projectDirectory.asFile
                .resolve("src/generated/jvmMain/resources/mecab-$dicType")
        destDir.mkdirs()

        listOf("sys.dic", "unk.dic", "char.bin", "matrix.bin").forEach { file ->
            // copy to src/generated/jvmMain/resources/
            val src = mecabDictDir.resolve(file)
            val dest = destDir.resolve(file)
            println("Copying $src to $dest")
            src.copyTo(dest, overwrite = true)
        }
    }
}
