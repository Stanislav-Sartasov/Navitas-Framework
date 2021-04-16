package com.example.ui_testing_samples

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

import java.io.IOException
import java.io.DataOutputStream

// Specific for Samsung A3 (2016) with Exynos 7580
private var MIN_FREQ = 400000
private var MAX_FREQ = 1500000

private fun executeCMDWrite(cmd: String) {
    try {
        val p = Runtime.getRuntime().exec("su")
        val dos = DataOutputStream(p.outputStream)
        
        dos.writeBytes("$cmd\nexit\n")
        dos.flush()
        dos.close()
        p.waitFor()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

@RunWith(AndroidJUnit4::class)
class InliningSeparateClassTest {

    @Test
    fun start() {
        ActivityScenario.launch(MainActivity::class.java)

        executeCMDWrite("echo " + MAX_FREQ + " > /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");

        val calc = Thread(CalcTask())

        calc.start()

        try {
            calc.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        executeCMDWrite("echo " + MIN_FREQ + " > /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
    }
}

private class CalcTask() : Runnable {
    private var cycles: Int = 1000000;

    private fun calcFunc() {
        System.currentTimeMillis();
    }

    override fun run() {
        for (i in 0 until cycles) {
            calcFunc()
            calcFunc()
            calcFunc()
            calcFunc()
            calcFunc()
            calcFunc()
            calcFunc()
            calcFunc()
            calcFunc()
            calcFunc()
        }
    }
}

@RunWith(AndroidJUnit4::class)
class InliningAnonymousClassTest {

    @Test
    fun start() {
        val calcTask : Runnable = object: Runnable
        {
            var cycles: Int = 1000000;

            fun calcFunc() {
                System.currentTimeMillis();
            }

            override fun run() {
                for (i in 0 until cycles) {
                    calcFunc()
                    calcFunc()
                    calcFunc()
                    calcFunc()
                    calcFunc()
                    calcFunc()
                    calcFunc()
                    calcFunc()
                    calcFunc()
                    calcFunc()
                }
            }
        }

        ActivityScenario.launch(MainActivity::class.java)

        executeCMDWrite("echo " + MAX_FREQ + " > /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");

        val calc = Thread(calcTask)

        calc.start()

        try {
            calc.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        executeCMDWrite("echo " + MIN_FREQ + " > /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
    }
}