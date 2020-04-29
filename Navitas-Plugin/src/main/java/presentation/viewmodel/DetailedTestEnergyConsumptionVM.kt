package presentation.viewmodel

import data.model.MethodDetails
import domain.model.DetailedTestEnergyConsumption
import domain.model.EnergyConsumption
import domain.model.TestInfo
import domain.repository.ProfilingResultRepository
import extensions.toTreeNode
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.swing.tree.DefaultMutableTreeNode
import kotlin.math.abs

class DetailedTestEnergyConsumptionVM(
        private val profilingResultRepository: ProfilingResultRepository
) {

    companion object {
        val ALL_PROCESSES_AND_THREADS = -1 to -1
    }

    private val testInfoSubject = PublishSubject.create<TestInfo>()
    val testInfo: Observable<TestInfo> = testInfoSubject

    private val currentEnergyConsumptionSubject = PublishSubject.create<Pair<String, List<EnergyConsumption>>>()
    val currentEnergyConsumption: Observable<Pair<String, List<EnergyConsumption>>> = currentEnergyConsumptionSubject

    private val energyConsumptionTreeSubject = PublishSubject.create<DefaultMutableTreeNode>()
    val energyConsumptionTree: Observable<DefaultMutableTreeNode> = energyConsumptionTreeSubject

    private var cache: DetailedTestEnergyConsumption? = null
    private var currentProcessThreadIDs: Pair<Int, Int>? = null

    fun fetch(position: Int) {
        profilingResultRepository.fetchDetailedTestEnergyConsumption(position)
                .subscribe( { result ->
                    cache = result
                    val ids = result.testDetails.keys.toMutableList()
                    ids.add(0, ALL_PROCESSES_AND_THREADS)

                    // emission of initial data
                    fetch(ids[0])
                    testInfoSubject.onNext(TestInfo(result.testName, result.energy, ids))
                }, { error ->
                    // TODO: send error
                })
    }

    fun fetch(processThreadIDs: Pair<Int, Int>) {
        if (processThreadIDs != currentProcessThreadIDs) {
            currentProcessThreadIDs = processThreadIDs
            val root = createRoot(getCurrentExternalMethods())
            energyConsumptionTreeSubject.onNext(root)
        }
    }

    fun selectMethod(item: MethodDetails?) {
        val items = mutableListOf<EnergyConsumption>()
        val title: String
        if (item == null) {
            title = if (currentProcessThreadIDs == ALL_PROCESSES_AND_THREADS) cache!!.testName else "${cache!!.testName} (Process: ${currentProcessThreadIDs!!.first}, Thread: ${currentProcessThreadIDs!!.second})"
            for (child in getCurrentExternalMethods()) {
                items.add(EnergyConsumption(child.methodName, child.cpuEnergy))
            }
        } else {
            title = item.methodName
            var childrenEnergy = 0F
            for (child in item.nestedMethods) {
                items.add(EnergyConsumption(child.methodName, child.cpuEnergy))
                childrenEnergy += child.cpuEnergy
            }
            items.add(EnergyConsumption(item.methodName, abs(item.cpuEnergy - childrenEnergy))) // TODO: ISSUE: remaining energy may be negative
        }
        currentEnergyConsumptionSubject.onNext(title to items)
    }

    private fun createRoot(children: List<MethodDetails>): DefaultMutableTreeNode {
        val root = DefaultMutableTreeNode(MethodDetails("", 0, 0, 0F, emptyList()))
        for (item in children) root.add(item.toTreeNode())
        return root
    }

    private fun getCurrentExternalMethods(): List<MethodDetails> {
        return if (currentProcessThreadIDs == ALL_PROCESSES_AND_THREADS) {
            cache!!.testDetails.values.flatten()
        } else {
            cache!!.testDetails.getOrDefault(currentProcessThreadIDs, emptyList())
        }
    }
}
