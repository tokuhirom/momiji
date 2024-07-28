package io.github.tokuhirom.momiji.core.dict

data class DictNode(
    val surface: String,
    val token: Token,
    val feature: String,
) {
    override fun toString(): String =
        listOf(
            surface,
            token.lcAttr,
            token.rcAttr,
            token.wcost,
            feature,
        ).joinToString(",")
}
