package io.github.tokuhirom.momiji.binarydict

import okio.FileSystem
import okio.NodeJsFileSystem

internal actual fun getFileSystem(): FileSystem = NodeJsFileSystem
