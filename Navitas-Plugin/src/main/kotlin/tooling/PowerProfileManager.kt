package tooling

import domain.model.CpuCoreCluster
import domain.model.PowerProfile

class PowerProfileManager {
    // default profile initialization
    companion object {
        private const val defaultWifiOn = 0.1f
        private const val defaultWifiScan = 0.1f
        private const val defaultWifiActive = 0.1f
        private const val defaultBluetoothOn = 0.1f
        private const val defaultBluetoothActive = 0.1f

        private val defaultClusterFirst = CpuCoreCluster(4)
        private val defaultClusterSecond = CpuCoreCluster(4)

        // These values are taken from some device, so they are not default.
        // This is justified by the fact that this is the only way to calculate approximate values.
        init {
            defaultClusterFirst.speeds.add(400000)
            defaultClusterFirst.speeds.add(691200)
            defaultClusterFirst.speeds.add(806400)
            defaultClusterFirst.speeds.add(1017600)
            defaultClusterFirst.speeds.add(1190400)
            defaultClusterFirst.speeds.add(1305600)
            defaultClusterFirst.speeds.add(1382400)
            defaultClusterFirst.speeds.add(1401600)

            defaultClusterFirst.powers.add(45.21F)
            defaultClusterFirst.powers.add(53.6F)
            defaultClusterFirst.powers.add(58.13F)
            defaultClusterFirst.powers.add(69.04F)
            defaultClusterFirst.powers.add(93.88F)
            defaultClusterFirst.powers.add(101.37F)
            defaultClusterFirst.powers.add(105.93F)
            defaultClusterFirst.powers.add(109.05F)

            defaultClusterSecond.speeds.add(400000)
            defaultClusterSecond.speeds.add(883200)
            defaultClusterSecond.speeds.add(940800)
            defaultClusterSecond.speeds.add(998400)
            defaultClusterSecond.speeds.add(1056000)
            defaultClusterSecond.speeds.add(1113600)
            defaultClusterSecond.speeds.add(1190400)
            defaultClusterSecond.speeds.add(1248000)
            defaultClusterSecond.speeds.add(1305600)
            defaultClusterSecond.speeds.add(1382400)
            defaultClusterSecond.speeds.add(1612800)
            defaultClusterSecond.speeds.add(1747200)
            defaultClusterSecond.speeds.add(1804800)

            defaultClusterSecond.powers.add(77.22F)
            defaultClusterSecond.powers.add(110.86F)
            defaultClusterSecond.powers.add(118.68F)
            defaultClusterSecond.powers.add(122.8F)
            defaultClusterSecond.powers.add(127.32F)
            defaultClusterSecond.powers.add(133.2F)
            defaultClusterSecond.powers.add(144.33F)
            defaultClusterSecond.powers.add(159.41F)
            defaultClusterSecond.powers.add(164.03F)
            defaultClusterSecond.powers.add(172.87F)
            defaultClusterSecond.powers.add(232.71F)
            defaultClusterSecond.powers.add(258.17F)
            defaultClusterSecond.powers.add(275.35F)
        }

        private val defaultProfile = PowerProfile (
            "Default Profile",
            arrayListOf(defaultClusterFirst, defaultClusterSecond),
            defaultWifiOn,
            defaultWifiScan,
            defaultWifiActive,
            defaultBluetoothOn,
            defaultBluetoothActive
        )

        fun getDefaultProfile(): PowerProfile {
            return defaultProfile
        }
    }
}