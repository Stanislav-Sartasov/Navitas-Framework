package tooling

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content
import presentation.view.common.ContentContainer
import javax.inject.Provider

class ContentRouterImpl(
        private val toolWindow: ToolWindow
) : ContentRouter {

    private val contentStack = mutableListOf<Content>()
    private lateinit var providers: List<Provider<ContentContainer>>

    fun setupProviders(_providers: List<Provider<ContentContainer>>) {
        providers = _providers
    }

    override fun toNextContent(arg: Any?) {
        if (contentStack.size < providers.size) {
            with(toolWindow.contentManager) {
                val container = providers[contentStack.size].get()
                arg?.let {
                    container.setArgument(it)
                }
                val newContent = factory.createContent(container.panel, "", false)
                if (contentStack.isNotEmpty()) {
                    removeContent(contentStack.last(), false)
                }
                contentStack.add(newContent)
                addContent(newContent)
            }
        }
    }

    override fun toPreviousContent() {
        if (contentStack.size > 1) {
            with(toolWindow.contentManager) {
                removeContent(contentStack.last(), true)
                contentStack.removeAt(contentStack.size - 1)
                addContent(contentStack.last())
            }
        }
    }
}
