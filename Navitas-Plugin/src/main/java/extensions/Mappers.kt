package extensions

import data.model.MethodDetails
import javax.swing.tree.DefaultMutableTreeNode

fun MethodDetails.toTreeNode(): DefaultMutableTreeNode {
    val rootNode = DefaultMutableTreeNode(this)
    for (child in nestedMethods) {
        rootNode.add(child.toTreeNode())
    }
    return rootNode
}