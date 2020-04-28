package presentation.view.common

import javax.swing.JPanel

// TODO: move common code from ContentView to here
abstract class ContentContainer {

    // UI components
    abstract val panel: JPanel

    open fun setArgument(arg: Any) {}
}