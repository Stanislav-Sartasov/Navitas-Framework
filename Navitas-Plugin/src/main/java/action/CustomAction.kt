package action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

abstract class CustomAction : AnAction() {

    var isEnabled = true
    var isVisible = true

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabled = isEnabled
        e.presentation.isVisible = isVisible
    }
}