package io.github.tokuhirom.momiji.binarydict

import io.github.tokuhirom.momiji.core.CostManager
import io.github.tokuhirom.momiji.core.LatticeBuilder
import io.github.tokuhirom.momiji.core.character.CharMap
import io.github.tokuhirom.momiji.core.dict.Dict
import io.github.tokuhirom.momiji.core.matrix.Matrix
import io.github.tokuhirom.momiji.core.unknown.DefaultUnknownWordDetector
import okio.FileSystem
import okio.Path.Companion.toPath

internal expect fun getFileSystem(): FileSystem

class MomijiBinaryDictLoader(
    private val directory: String,
) {
    fun load(): LatticeBuilder {
        val sys = loadSysDic()
        val matrix = loadMatrix()
        val charMap = loadCharMap()
        val unknown = loadUnknown()

        val costManager = CostManager(matrix)
        val unknownWordDetector = DefaultUnknownWordDetector(charMap, unknown)
        return LatticeBuilder(sys, costManager, unknownWordDetector)
    }

    fun loadMatrix(): Matrix {
        val path = directory.toPath().resolve("matrix.bin")
        return getFileSystem().read(path) {
            Matrix.parseBinary(this.readByteArray())
        }
    }

    fun loadCharMap(): CharMap {
        var path = directory.toPath().resolve("char.bin")
        return getFileSystem().read(path) {
            CharMap.parseBinary(this.readByteArray())
        }
    }

    fun loadSysDic(): Dict {
        var path = directory.toPath().resolve("sys.dic")
        return getFileSystem().read(path) {
            Dict.parseBinary(this.readByteArray())
        }
    }

    fun loadUnknown(): Dict {
        var path = directory.toPath().resolve("unk.dic")
        return getFileSystem().read(path) {
            Dict.parseBinary(this.readByteArray())
        }
    }
}
