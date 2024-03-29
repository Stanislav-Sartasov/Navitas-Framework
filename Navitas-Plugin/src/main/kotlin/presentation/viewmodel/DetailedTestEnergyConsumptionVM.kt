package presentation.viewmodel

import domain.model.CpuMethodEnergyConsumption
import domain.model.DetailedTestEnergyConsumption
import domain.model.EnergyConsumption
import domain.repository.ProfilingResultRepository
import extensions.roundWithAccuracy
import extensions.toTreeNode
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.swing.tree.DefaultMutableTreeNode

class DetailedTestEnergyConsumptionVM(
        private val profilingResultRepository: ProfilingResultRepository
) {

    companion object {
        val ALL_PROCESSES_AND_THREADS = -1 to -1
    }

    private val threadProcessIDsSubject = PublishSubject.create<List<Pair<Int, Int>>>()
    val threadProcessIDs: Observable<List<Pair<Int, Int>>> = threadProcessIDsSubject

    private val currentEnergyConsumptionSubject = PublishSubject.create<Pair<String, List<EnergyConsumption>>>()
    val currentEnergyConsumption: Observable<Pair<String, List<EnergyConsumption>>> = currentEnergyConsumptionSubject

    private val energyConsumptionTreeSubject = PublishSubject.create<DefaultMutableTreeNode>()
    val energyConsumptionTree: Observable<DefaultMutableTreeNode> = energyConsumptionTreeSubject

    var cache: DetailedTestEnergyConsumption? = null
    private var currentProcessThreadIDs: Pair<Int, Int>? = null

    fun fetch(position: Int) {
        profilingResultRepository.fetchDetailedTestEnergyConsumption(position)
                .subscribe( { result ->
                    cache = result
                    val ids = result.cpuEnergyConsumption.testDetails.keys.toMutableList()
                    ids.add(0, ALL_PROCESSES_AND_THREADS)

                    // emission of initial data
                    fetch(ids[0])
                    threadProcessIDsSubject.onNext(ids)
                }, { error ->
                    error.printStackTrace()
                })
    }

    fun fetch(processThreadIDs: Pair<Int, Int>) {
        if (processThreadIDs != currentProcessThreadIDs) {
            currentProcessThreadIDs = processThreadIDs
            val root = createRoot(getCurrentExternalMethods())
            energyConsumptionTreeSubject.onNext(root)
        }
    }

    fun selectMethod(item: CpuMethodEnergyConsumption?) {
        val items = mutableListOf<EnergyConsumption>()
        val title: String
        if (item == null) {
            title = if (currentProcessThreadIDs == ALL_PROCESSES_AND_THREADS) cache!!.testName else "${cache!!.testName} (Process: ${currentProcessThreadIDs!!.first}, Thread: ${currentProcessThreadIDs!!.second})"
            for (child in getCurrentExternalMethods()) {
                items.add(EnergyConsumption(child.methodName, child.cpuEnergy, Float.NaN, Float.NaN, Float.NaN))
            }
        } else {
            title = item.methodName
            var childrenEnergy = 0F
            for (child in item.nestedMethods) {
                items.add(EnergyConsumption(child.methodName, child.cpuEnergy, Float.NaN, Float.NaN, Float.NaN))
                childrenEnergy += child.cpuEnergy
            }
            val remainder = (item.cpuEnergy - childrenEnergy).roundWithAccuracy(1)
            items.add(EnergyConsumption(item.methodName, remainder, Float.NaN, Float.NaN, Float.NaN))
        }
        currentEnergyConsumptionSubject.onNext(title to items)
    }

    private fun createRoot(children: List<CpuMethodEnergyConsumption>): DefaultMutableTreeNode {
        val root = DefaultMutableTreeNode(CpuMethodEnergyConsumption("", 0, 0, 0F, emptyList()))
        for (item in children) root.add(item.toTreeNode())
        return root
    }

    private fun getCurrentExternalMethods(): List<CpuMethodEnergyConsumption> {
        return if (currentProcessThreadIDs == ALL_PROCESSES_AND_THREADS) {
            cache!!.cpuEnergyConsumption.testDetails.values.flatten()
        } else {
            cache!!.cpuEnergyConsumption.testDetails.getOrDefault(currentProcessThreadIDs, emptyList())
        }
    }
}
