package data.model

class PowerProfile(
        val path: String,
        private val clusters: List<Cluster>
) {

    fun getPowerAtSpeed(coreIndex: Int, speed: Long): Float {
        val cluster = getClusterWithCore(coreIndex)
        val speeds = cluster!!.speeds
        val powers = cluster.powers

        if (speeds.contains(speed)) {
            return powers[speeds.indexOf(speed)]
        } else {
            // if speed is higher/lower than all speeds in power profile, return max/min power
            if (speed < speeds[0]) {
                return powers[0]
            }
            if (speed > speeds[speeds.size - 1]) {
                return powers[powers.size - 1]
            }

            var i = 0
            var currSpeed = speeds[i]
            while (currSpeed < speed) {
                currSpeed = speeds[++i]
            }

            val upperSpeed = speeds[i]
            val lowerSpeed = speeds[i - 1]
            val upperPower = powers[i]
            val lowerPower = powers[i - 1]

            return ((upperPower - lowerPower) * (speed - lowerSpeed)) / (upperSpeed - lowerSpeed)
        }
    }

    private fun getClusterWithCore(coreIndex: Int): Cluster? {
        var index = coreIndex
        for (cluster in clusters) {
            if (cluster.numCores - 1 >= index) {
                return cluster
            } else {
                index -= cluster.numCores
            }
        }
        return null
    }
}