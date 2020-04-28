package com.example.ui_testing_samples

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnotherTest {

    @Test
    fun mainActivity_start() {
        ActivityScenario.launch(MainActivity::class.java)
    }
}