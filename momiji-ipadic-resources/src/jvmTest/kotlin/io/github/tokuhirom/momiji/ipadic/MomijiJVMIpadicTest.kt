package io.github.tokuhirom.momiji.ipadic

import kotlin.test.Test

class MomijiJVMIpadicTest {
    @Test
    fun testMomijiLoadSysDic() {
        momijiLoadSysDic()
    }

    @Test
    fun testMomijiLoadMatrix() {
        momijiLoadMatrix()
    }

    @Test
    fun testMomijiLoadCharMap() {
        momijiLoadCharMap()
    }

    @Test
    fun testMomijiLoadUnknown() {
        momijiLoadUnknown()
    }
}
