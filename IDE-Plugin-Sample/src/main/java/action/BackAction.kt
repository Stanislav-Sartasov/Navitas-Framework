package action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import tooling.ContentRouter
import tooling.OnBackClickCallback

class BackAction(
    private val router: ContentRouter? = null,
    private val callback: OnBackClickCallback? = null
) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        callback?.let {
            if (callback.onBackClick()) return
        }
        router?.toPreviousContent()
    }
}