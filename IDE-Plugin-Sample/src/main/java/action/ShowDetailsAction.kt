package action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import tooling.ActionState
import tooling.OnActionClickCallback

class ShowDetailsAction(
        private val callback: OnActionClickCallback? = null
) : AnAction(), ActionState {

    override var isEnabled = true
    override var isVisible = true

    override fun actionPerformed(e: AnActionEvent) {
        callback?.onActionClick()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabled = isEnabled
        e.presentation.isVisible = isVisible
    }
}