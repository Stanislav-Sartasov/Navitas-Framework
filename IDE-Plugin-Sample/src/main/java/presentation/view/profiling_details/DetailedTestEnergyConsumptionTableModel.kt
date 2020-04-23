package presentation.view.profiling_details

import data.model.MethodDetails
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class DetailedTestEnergyConsumptionTableModel(private val items: List<MethodDetails>) : TableModel {

    override fun getRowCount() = items.size

    override fun getColumnCount() = 2

    override fun getColumnName(var1: Int): String? {
        return when (var1) {
            0 -> "Method"
            1 -> "Energy (mJ)"
            else -> null
        }
    }

    override fun getColumnClass(var1: Int): Class<*>? = String::class.java

    override fun isCellEditable(var1: Int, var2: Int) = false

    override fun getValueAt(var1: Int, var2: Int): Any? {
        if (var2 == 0) return items[var1].methodName
        if (var2 == 1) return items[var1].cpuEnergy
        return null
    }

    override fun setValueAt(var1: Any?, var2: Int, var3: Int) {}

    override fun addTableModelListener(var1: TableModelListener?) {}

    override fun removeTableModelListener(var1: TableModelListener?) {}
}