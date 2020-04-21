package data

import domain.model.EnergyConsumption
import domain.model.TestEnergyConsumption
import kotlin.random.Random

fun generateMethodEnergyConsumption(): EnergyConsumption {
    val consumer = String(Random.nextBytes(16), Charsets.UTF_16)
    val energy = Random.nextFloat() * Random.nextInt(1, 100)
    return EnergyConsumption(consumer, energy)
}

fun generateTestEnergyConsumption(): TestEnergyConsumption {
    val consumer = String(Random.nextBytes(16), Charsets.UTF_16)
    val k = Random.nextInt(5, 100)
    val details = mutableListOf<EnergyConsumption>()
    for (i in 0 until k) details.add(generateMethodEnergyConsumption())
    return TestEnergyConsumption(consumer, details)
}

fun generateTestEnergyConsumptionList(amount: Int): List<TestEnergyConsumption> {
    val result = mutableListOf<TestEnergyConsumption>()
    for (i in 0 until amount) result.add(generateTestEnergyConsumption())
    return result
}