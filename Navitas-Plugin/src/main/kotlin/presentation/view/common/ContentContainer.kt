package presentation.view.common

import javax.swing.JPanel

abstract class ContentContainer {

    // UI components
    abstract val panel: JPanel

    open fun setArgument(arg: Any) {}
}