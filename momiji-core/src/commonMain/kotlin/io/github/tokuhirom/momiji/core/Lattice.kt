package io.github.tokuhirom.momiji.core

import io.github.tokuhirom.momiji.core.dict.DictNode

/**
 * Every index should be the character index.
 */
class Lattice(
    private val sentence: String,
    private val costManager: CostManager,
) {
    private val beginNodes: MutableList<MutableList<Node>> = mutableListOf()
    private val endNodes: MutableList<MutableList<Node>> = mutableListOf()

    init {
        // +1 for EOS node
        for (i in 0 until sentence.length + 1) {
            beginNodes.add(mutableListOf())
            endNodes.add(mutableListOf())
        }

        // register BOS node
        endNodes[0].add(Node.BOS())

        // register EOS node
        beginNodes[sentence.length].add(Node.EOS())
    }

    /**
     * Insert a new node to the lattice.
     */
    internal fun insert(
        begin: Int,
        end: Int,
        dictRow: DictNode? = null,
    ): Node {
        val node =
            Node.Word(
                surface = sentence.substring(begin, end),
                length = end - begin,
                dictRow = dictRow,
            )
        beginNodes[begin].add(node)
        endNodes[end].add(node)
        return node
    }

    /**
     * Viterbi algorithm.
     *
     * @return List of nodes from BOS to EOS.
     */
    fun viterbi(): List<Node> {
        val bos = endNodes[0][0]
        bos.minPrev = null
        bos.minCost = 0

        for (i in 0 until sentence.length + 1) {
            for (rnode in beginNodes[i]) {
                rnode.minPrev = null
                rnode.minCost = Int.MAX_VALUE

                for (lnode in endNodes[i]) {
                    val cost =
                        lnode.minCost +
                            costManager.getTransitionCost(lnode, rnode).toInt() + // transition cost
                            costManager.getEmissionCost(rnode) // emission cost
                    if (cost < rnode.minCost) {
                        rnode.minCost = cost
                        rnode.minPrev = lnode
                    }
                }
            }
        }

        // minPrev を EOS から BOS までたどり、最後に反転する
        val results = mutableListOf<Node>()
        val eos = beginNodes[sentence.length][0]
        var node: Node? = eos
        while (node != null) {
            results.add(node)
            node = node.minPrev
        }
        return results.reversed()
    }

    /**
     * Export lattice to DOT format.
     */
    fun toDot(): String {
        val sb = StringBuilder()
        sb.append("digraph lattice {\n")
        sb.append("rankdir=LR;\n")

        val bos = endNodes[0][0]
        sb.append("node_${bos.hashCode().toUInt()} [label=\"__BOS__\"];\n")

        // ノードのエクスポート
        for (i in beginNodes.indices) {
            for (node in beginNodes[i]) {
                val label = node.surface.replace("\"", "\\\"") + node.dictRow?.feature
                sb.append("node_${node.hashCode().toUInt()} [label=\"$label (${node.dictRow?.token?.wcost ?: "?"})\"];\n")
            }
        }

        // エッジのエクスポート
        for (i in beginNodes.indices) {
            sb.append("/* $i */\n")
            for (rnode in beginNodes[i]) {
                sb.append("  /* rnode:${rnode.surface} */\n")
                for (lnode in endNodes[i]) {
                    val transitionCost = costManager.getTransitionCost(lnode, rnode).toInt()
                    val emissionCost = costManager.getEmissionCost(rnode)
                    val totalCost = transitionCost + emissionCost
                    sb.append(
                        "    /* lnode:${lnode.surface}(rid=${lnode.dictRow?.token?.rcAttr}) rnode:${rnode.surface}(lid=${rnode.dictRow?.token?.lcAttr}) */\n",
                    )
                    sb.append(
                        "    node_${lnode.hashCode().toUInt()} -> node_${rnode.hashCode().toUInt()} [label=\"$totalCost\"];\n",
                    )
                }
            }
        }

        sb.append("}\n")
        return sb.toString()
    }
}
