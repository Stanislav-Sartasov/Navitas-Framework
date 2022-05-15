package presentation.view.profiling_details

import domain.model.EnergyConsumption
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class TestEnergyConsumptionTableModel(private val items: List<EnergyConsumption>) : TableModel {

    override fun getRowCount() = items.size

    override fun getColumnCount() = 7

    override fun getColumnName(var1: Int): String? {
        return when (var1) {
            0 -> "Test"
            1 -> "CPU (mAh)"
            2 -> "Wi-Fi (mAh)"
            3 -> "Bluetooth (mAh)"
            4 -> "Wifi connection memory used (KB)"
            5 -> "Wifi connection packets"
            6 -> "Wifi connection energy used (mAh)"
            else -> null
        }
    }

    override fun getColumnClass(var1: Int): Class<*>? = String::class.java

    override fun isCellEditable(var1: Int, var2: Int) = false

    override fun getValueAt(var1: Int, var2: Int): Any? {
        if (var2 == 0) return items[var1].consumer
        if (var2 == 1) return items[var1].cpuEnergy
        if (var2 == 2) return items[var1].wifiEnergy
        if (var2 == 3) return items[var1].bluetoothEnergy
        if (var2 == 4) return items[var1].memory
        if (var2 == 5) return items[var1].packets
        if (var2 == 6) return items[var1].energy
        return null
    }

    override fun setValueAt(var1: Any?, var2: Int, var3: Int) {}

    override fun addTableModelListener(var1: TableModelListener?) {}

    override fun removeTableModelListener(var1: TableModelListener?) {}
}