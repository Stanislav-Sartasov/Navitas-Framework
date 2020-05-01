package presentation.view.charts

import domain.model.EnergyConsumption
import org.knowm.xchart.*
import javax.swing.JPanel
import org.knowm.xchart.style.Styler
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class ChartPanel : JPanel() {
    private enum class ChartType {
        BAR_CHART, PIE_CHART
    }

    private val defaultTitle = "Energy consumption"
    private val yAxisTitle   = "Energy (mJ)"

    fun showChart(data : List<EnergyConsumption>, title: String = defaultTitle) {
        val panel = setupNewChart(
                input = data,
                title = title,
                type = if (title == defaultTitle) ChartType.BAR_CHART
                       else ChartType.PIE_CHART
        )
        addToView(panel)
    }

    private fun setupNewChart(input: List<EnergyConsumption>, title: String,
                              type: ChartType) : JPanel {
        //Chart panel can't be smaller than chartHeight*chartWidth
        val chartHeight = 0
        val chartWidth  = 0

        val data = mergeSameNames(input)

        val chartBuilder = when (type) {
            ChartType.BAR_CHART -> CategoryChartBuilder()
                    .yAxisTitle(yAxisTitle)
            ChartType.PIE_CHART -> PieChartBuilder()
        }

        val chart = chartBuilder
                .width(chartWidth)
                .height(chartHeight)
                .title(title)
                .build()

        chart.styler.isLegendVisible = true
        chart.styler.legendPosition = Styler.LegendPosition.OutsideE

        when (chart) {
            is CategoryChart -> {
                //Otherwise 0 will be displayed on the X axis
                val xAxisOverride = mapOf(.0 to " ")
                chart.setXAxisLabelOverrideMap(xAxisOverride)

                for (elem in data)
                    chart.addSeries(elem.consumer,
                            doubleArrayOf(data.indexOf(elem).toDouble()),
                            doubleArrayOf(elem.energy.toDouble()))
            }
            is PieChart -> {
                for ((name, value) in data)
                    chart.addSeries(name, value)
            }
        }

        return XChartPanel(chart)
    }

    private fun mergeSameNames(data: List<EnergyConsumption>) : List<EnergyConsumption> {
        val result = mutableMapOf<String, Float>()
        for (elem in data)
            if (!result.containsKey(elem.consumer))
                result[elem.consumer] = elem.energy
            else
                //Weird error when result[elem.consumer] += elem.energy
                result[elem.consumer] = result[elem.consumer]!! + elem.energy

        return result.map { (name, value) ->
            EnergyConsumption(name, value)
        }
    }

    private fun addToView(panel : JPanel) {
        removeAll()
        this.layout = GridBagLayout()

        val constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.BOTH
        constraints.gridx = 1
        constraints.gridy = 1
        constraints.weightx = 1.0
        constraints.weighty = 1.0

        panel.layout = BorderLayout()
        add(panel, constraints)
    }
}