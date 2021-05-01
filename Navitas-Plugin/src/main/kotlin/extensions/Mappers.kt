package extensions

import domain.model.CpuMethodEnergyConsumption
import javax.swing.tree.DefaultMutableTreeNode

fun CpuMethodEnergyConsumption.toTreeNode(): DefaultMutableTreeNode {
    val rootNode = DefaultMutableTreeNode(this)
    for (child in nestedMethods) {
        rootNode.add(child.toTreeNode())
    }
    return rootNode
}