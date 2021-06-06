package com.example.navi_test

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class MultithreadingTest {

    @Test
    fun startWithoutSort() {
        ActivityScenario.launch(MainActivity::class.java)

        val testClass = ThreadCreator()

        testClass.initiateCalcFuncWithoutSort().join()
        testClass.initiateCalcFuncWithoutSort().join()
        testClass.initiateCalcFuncWithoutSort().join()
        testClass.initiateCalcFuncWithoutSort().join()
        testClass.initiateCalcFuncWithoutSort().join()
    }

    @Test
    fun startWithSort() {
        ActivityScenario.launch(MainActivity::class.java)

        val testClass = ThreadCreator()

        testClass.initiateCalcFuncWithSort().join()
        testClass.initiateCalcFuncWithSort().join()
        testClass.initiateCalcFuncWithSort().join()
        testClass.initiateCalcFuncWithSort().join()
        testClass.initiateCalcFuncWithSort().join()
    }
}

private class ThreadCreator {
    fun initiateCalcFuncWithoutSort(): Thread {
        val thread = Thread(Runnable{calcTaskWithoutSort()})

        thread.start()
        Thread.sleep(5)

        return thread
    }

    fun initiateCalcFuncWithSort(): Thread {
        val thread = Thread(Runnable{calcTaskWithSort()})

        thread.start()
        Thread.sleep(5)

        return thread
    }
}

private fun calcTaskWithoutSort() {
    val array = IntArray(1000000) { Random(0).nextInt() }
    val path = System.getProperty("user.dir")
    val file = File("$path/tmp.txt")
    file.printWriter().use{ out -> out.println(array) }
    file.delete()
}

private fun calcTaskWithSort() {
    val array = IntArray(1000000) { Random(0).nextInt() }
    val path = System.getProperty("user.dir")
    array.sort()
    val file = File("$path/tmp.txt")
    file.printWriter().use{ out -> out.println(array) }
    file.delete()
}