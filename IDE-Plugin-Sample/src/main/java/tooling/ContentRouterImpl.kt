package tooling

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content

class ContentRouterImpl(private val toolWindow: ToolWindow) : ContentRouter {

    private var currentContentIndex = 0
    private var currentContent: Content? = null
    private lateinit var contentList: List<Content>

    fun setupContents(_contents: List<Content>) {
        currentContentIndex = 0
        currentContent = null
        contentList = _contents
    }

    override fun toNextContent() {
        if (currentContentIndex < contentList.size - 1) {
            with(toolWindow.contentManager) {
                currentContent?.apply {
                    removeContent(this, false)
                    currentContentIndex++
                }
                currentContent = contentList[currentContentIndex].also { content -> addContent(content) }
            }
        }
    }

    override fun toPreviousContent() {
        if (currentContentIndex > 0) {
            with(toolWindow.contentManager) {
                currentContent?.apply {
                    removeContent(this, false)
                    currentContentIndex--
                }
                currentContent = contentList[currentContentIndex].also { content -> addContent(content) }
            }
        }
    }
}