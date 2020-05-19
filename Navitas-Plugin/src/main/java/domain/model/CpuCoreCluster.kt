package domain.model

class CpuCoreCluster(val numCores: Int) {
    val speeds = ArrayList<Long>()
    val powers = ArrayList<Float>()
}