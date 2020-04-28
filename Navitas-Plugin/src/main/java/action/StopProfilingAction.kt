package action

import com.intellij.openapi.actionSystem.AnActionEvent
import tooling.OnActionClickCallback

class StopProfilingAction(
        private val callback: OnActionClickCallback? = null
) : CustomAction() {

    override fun actionPerformed(e: AnActionEvent) {
        callback?.onActionClick()
    }
}