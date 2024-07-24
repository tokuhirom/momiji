package io.github.tokuhirom.momiji.binarydict

import io.github.tokuhirom.momiji.engine.LatticeBuilder
import io.github.tokuhirom.momiji.engine.src.CharMap
import io.github.tokuhirom.momiji.engine.src.Dict
import io.github.tokuhirom.momiji.engine.src.Matrix
import okio.FileSystem
import okio.Path.Companion.toPath

internal expect fun getFileSystem(): FileSystem

class MomijiBinaryDictLoader(
    private val directory: String,
) {
    fun load(): LatticeBuilder {
        TODO()
    }

    fun loadMatrix(): Matrix {
        val path = directory.toPath().resolve("matrix.bin")
        return getFileSystem().read(path) {
            Matrix.parseBinary(this.readByteArray())
        }
    }

    fun loadCharMap(): CharMap {
        TODO("Not yet implemented")
    }

    fun loadDict(): Dict {
        TODO("Not yet implemented")
    }

    fun loadUnknown(): Dict {
        TODO("Not yet implemented")
    }
}
