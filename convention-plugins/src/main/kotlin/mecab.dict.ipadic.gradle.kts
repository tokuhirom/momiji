import io.github.tokuhirom.kdary.KDary
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

open class BuildDictTask : DefaultTask() {
    private val url = "https://drive.google.com/uc?export=download&id=0B4y35FiV1wh7MWVlSDBCSXZMTXM"
    private val tarball = "mecab-ipadic.tar.gz"

    @TaskAction
    fun run() {
        project.layout.projectDirectory
            .asFile
            .resolve("src/generated/commonMain/kotlin/io/github/tokuhirom/momiji/ipadic/")
            .deleteRecursively()

        // mkdir -p build
        project.layout.buildDirectory
            .get()
            .asFile
            .mkdirs()

        download()
        val mecabDictDir = extract()
        buildDict(mecabDictDir)
        val wordEntries = convertFiles(mecabDictDir)
        buildKdary(wordEntries)
        copyFiles(mecabDictDir)
    }

    private fun download() {
        runBlocking {
            val client = HttpClient()
            val response = client.get(url)
            val file = File(tarball)
            response.bodyAsChannel().copyTo(file.outputStream())
            println("Downloaded $tarball")
        }
    }

    private fun extract(): Path {
        val buildDir =
            project.layout.buildDirectory
                .get()
                .asFile

        val processBuilder = ProcessBuilder("tar", "-xzvf", tarball, "-C", buildDir.absolutePath)
        val process = processBuilder.start()
        process.waitFor()
        println("Extracted to $buildDir")
        return buildDir.toPath().resolve("mecab-ipadic-2.7.0-20070801")
    }

    private fun buildDict(dictDir: Path) {
        listOf(
            listOf("./configure", "--with-charset", "utf-8"),
            listOf("make"),
        ).forEach { command ->
            val processBuilder =
                ProcessBuilder(command)
                    .directory(dictDir.toFile())
                    .redirectErrorStream(true)
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val status = process.waitFor()
            println("$command: $status: $output")
        }
    }

    private fun convertFiles(mecabDictDir: Path): List<io.github.tokuhirom.momiji.gradle.CsvRow> {
        val csvFiles =
            mecabDictDir.toFile().listFiles { _, name ->
                name.endsWith(".csv")
            }
        checkNotNull(csvFiles) {
            "csvFiles must not null"
        }
        val eucJpCharset = Charset.forName("EUC-JP")
        val lines =
            csvFiles
                .flatMap { it.readLines(eucJpCharset) }
                .map {
                    io.github.tokuhirom.momiji.gradle.CsvRow
                        .parse(it)
                }.sortedBy { it.surface }

        val outputCsv =
            project.layout.projectDirectory
                .asFile
                .resolve("src/generated/commonMain/kotlin/io/github/tokuhirom/momiji/ipadic/dictcsv")

        writeChunks(
            outputCsv,
            src = lines.joinToString("\n") { it.raw },
            pkg = "io.github.tokuhirom.momiji.ipadic.dictcsv",
            filePrefix = "DictCsv",
            variablePrefix = "DICT_CSV",
        )
        println("Converted to $outputCsv")

        return lines
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun buildKdary(wordEntries: List<io.github.tokuhirom.momiji.gradle.CsvRow>) {
        val kdary = KDary.build(wordEntries.map { it.surface.toByteArray(Charsets.UTF_8) })
        val byteArray = kdary.toByteArray()

        val baseDir =
            project.layout.projectDirectory
                .asFile
                .resolve("src/generated/commonMain/kotlin/io/github/tokuhirom/momiji/ipadic/kdary")

        val src = Base64.encode(byteArray)
        writeChunks(
            baseDir,
            src,
            pkg = "io.github.tokuhirom.momiji.ipadic.kdary",
            filePrefix = "KDary",
            variablePrefix = "KDARY_BASE64",
        )

        println("Wrote dictionary to $baseDir")
    }

    private fun writeChunks(
        baseDir: File,
        src: String,
        pkg: String,
        filePrefix: String,
        variablePrefix: String,
    ) {
        baseDir.mkdirs()

        // 1024 -> Matrix
        // 65535 文字が最大っぽい。
        // https://stackoverflow.com/questions/62098263/kotlin-string-max-length-kotlin-file-with-a-long-string-is-not-compiling
        val chunks = splitStringByBytes(src)

        chunks.forEachIndexed { index, chunk ->
            baseDir.resolve("$filePrefix$index.kt").bufferedWriter().use { writer ->
                writer.write("@file:Suppress(\"ktlint:standard:max-line-length\")\n\n")
                writer.write("package $pkg\n\n")
                writer.write("internal const val ${variablePrefix}_$index = \"\"\"${escapeKotlinString(chunk)}\"\"\"\n")
                writer.newLine()
            }
        }

        baseDir.resolve("$filePrefix.kt").bufferedWriter().use { writer ->
            writer.write("package $pkg\n\n")
            writer.write("val $variablePrefix = ")
            writer.write(
                List(chunks.size) { index ->
                    "${variablePrefix}_$index"
                }.joinToString("+"),
            )
            writer.write("\n\n")
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun copyFiles(mecabDictDir: Path) {
        // char.def は text 形式のほうが小さいので text 形式を採用
        listOf("char.def", "unk.def").forEach { file ->
            val baseName = file.replace(".def", "")
            val sourceFile = File(mecabDictDir.toFile(), file)
            val baseDir =
                project.layout.projectDirectory
                    .asFile
                    .resolve("src/generated/commonMain/kotlin/io/github/tokuhirom/momiji/ipadic/$baseName")

            sourceFile.bufferedReader(Charset.forName("EUC-JP")).use { reader ->
                writeChunks(
                    baseDir,
                    src = reader.readText(),
                    pkg = "io.github.tokuhirom.momiji.ipadic.$baseName",
                    filePrefix =
                        baseName.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                    variablePrefix = baseName.uppercase(Locale.getDefault()),
                )
            }
            println("Copied $file")
        }

        // write matrix.bin
        // matrix.bin は明らかにバイナリ形式のほうが空間効率が良い。
        run {
            val sourceFile: File = mecabDictDir.toFile().resolve("matrix.bin")
            val baseDir =
                project.layout.projectDirectory
                    .asFile
                    .resolve("src/generated/commonMain/kotlin/io/github/tokuhirom/momiji/ipadic/matrix")

            val bytes = sourceFile.readBytes()
            val base64 = Base64.encode(bytes)
            writeChunks(
                baseDir,
                src = base64,
                pkg = "io.github.tokuhirom.momiji.ipadic.matrix",
                filePrefix = "Matrix",
                variablePrefix = "Matrix",
            )
            println("Copied matrix.bin")
        }
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
