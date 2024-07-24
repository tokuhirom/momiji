package io.github.tokuhirom.momiji.binarydict

import okio.FileSystem

internal actual fun getFileSystem(): FileSystem = FileSystem.SYSTEM
