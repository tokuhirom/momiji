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
    private val outputDir = "dict"

    @TaskAction
    fun run() {
        // mkdir $outputDir if it's not exists
        if (!File(outputDir).exists()) {
            File(outputDir).mkdir()
        }

        // mkdir -p build
        project.layout.buildDirectory
            .get()
            .asFile
            .mkdirs()

        download()
        val mecabDictDir = extract()
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
            pkg = "io.github.tokuhirom.momiji.dictcsv",
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

        val chunks = src.chunked(64 * 1024)
        chunks.forEachIndexed { index, chunk ->
            baseDir.resolve("$filePrefix$index.kt").bufferedWriter().use { writer ->
                writer.write("package $pkg\n\n")
                writer.write("internal val ${variablePrefix}_$index = \"\"\"${escapeKotlinString(chunk)}\"\"\"\n")
                writer.newLine()
            }
        }

        baseDir.resolve("$filePrefix.kt").bufferedWriter().use { writer ->
            writer.write("package $pkg\n\n")
            writer.write("val $variablePrefix = listOf(\n")
            chunks.forEachIndexed { index, _ ->
                writer.write("    ${variablePrefix}_$index")
                if (index != chunks.size - 1) {
                    writer.write(",")
                }
                writer.newLine()
            }
            writer.write(").joinToString(\"\")\n")
        }
    }

    private fun copyFiles(mecabDictDir: Path) {
        listOf("matrix.def", "char.def", "unk.def").forEach { file ->
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
            println("Copied $file to $outputDir")
        }
    }

    private fun escapeKotlinString(src: String): String =
        src
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\$", "\${'\$'}")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
}
