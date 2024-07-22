import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.kdary.saveKDary
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

open class BuildDictTask : DefaultTask() {
    private val url = "https://drive.google.com/uc?export=download&id=0B4y35FiV1wh7MWVlSDBCSXZMTXM"
    private val tarball = "mecab-ipadic.tar.gz"
    private val outputDir = "dict"
    private val outputCsv = "$outputDir/momiji.csv"
    private val outputKdary = "$outputDir/momiji.kdary"

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

        File(outputCsv).writeText(lines.joinToString("\n"))
        println("Converted to $outputCsv")

        return lines
    }

    private fun buildKdary(wordEntries: List<io.github.tokuhirom.momiji.gradle.CsvRow>) {
        val kdary = KDary.build(wordEntries.map { it.surface.toByteArray(Charsets.UTF_8) })
        saveKDary(kdary, outputKdary)
        println("Built KDary dictionary at $outputKdary")
    }

    private fun copyFiles(mecabDictDir: Path) {
        listOf("matrix.def", "char.def", "unk.def").forEach { file ->
            val sourceFile = File(mecabDictDir.toFile(), file)
            val targetFile = File(outputDir, file)
            copyFileWithEncoding(sourceFile, targetFile, "EUC-JP", "UTF-8")
            println("Copied $file to $outputDir")
        }
    }

    private fun copyFileWithEncoding(
        source: File,
        target: File,
        sourceEncoding: String,
        targetEncoding: String,
    ) {
        val sourceCharset = Charset.forName(sourceEncoding)
        val targetCharset = Charset.forName(targetEncoding)

        source.bufferedReader(sourceCharset).use { reader ->
            target.bufferedWriter(targetCharset).use { writer ->
                reader.lineSequence().forEach { line ->
                    writer.write(line)
                    writer.newLine()
                }
            }
        }
    }
}
