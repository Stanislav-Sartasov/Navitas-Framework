package extensions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction

fun ActionManager.copyTemplate(actionId: String, action: AnAction) {
    val templateAction = getAction(actionId)
    action.copyFrom(templateAction)
}