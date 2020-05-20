package tooling

interface ContentRouter {

    fun toNextContent(arg: Any? = null)
    fun toPreviousContent()
}