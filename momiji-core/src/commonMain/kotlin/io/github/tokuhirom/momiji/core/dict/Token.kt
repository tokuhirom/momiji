package io.github.tokuhirom.momiji.core.dict

/*
struct Token {
  unsigned short lcAttr;
  unsigned short rcAttr;
  unsigned short posid;
  short wcost;
  unsigned int   feature;
  unsigned int   compound;
};
 */
class Token(
    val lcAttr: UShort,
    val rcAttr: UShort,
    val posid: UShort,
    val wcost: Short,
    val feature: UInt,
    val compound: UInt,
) {
    companion object {
        const val SIZE = 2 * 4 + 4 * 2
    }
}
