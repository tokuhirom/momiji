package io.github.tokuhirom.momiji.ipadic

import io.github.tokuhirom.momiji.core.character.CharMap
import io.github.tokuhirom.momiji.core.dict.Dict
import io.github.tokuhirom.momiji.core.matrix.Matrix

internal actual fun MomijiIpadicLoader.loadSysDic(): Dict = momijiLoadSysDic()

internal actual fun MomijiIpadicLoader.loadMatrix(): Matrix = momijiLoadMatrix()

internal actual fun MomijiIpadicLoader.loadCharMap(): CharMap = momijiLoadCharMap()

internal actual fun MomijiIpadicLoader.loadUnknown(): Dict = momijiLoadUnknown()
