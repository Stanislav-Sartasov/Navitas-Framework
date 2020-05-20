package extensions

import domain.model.MethodEnergyConsumption
import javax.swing.tree.DefaultMutableTreeNode

fun MethodEnergyConsumption.toTreeNode(): DefaultMutableTreeNode {
    val rootNode = DefaultMutableTreeNode(this)
    for (child in nestedMethods) {
        rootNode.add(child.toTreeNode())
    }
    return rootNode
}