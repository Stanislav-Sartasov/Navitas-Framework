package com.example.navi_constants

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WifiConstant {

    @Test
    fun startWifiOnTest() {
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(120000)
    }

    @Test
    fun startWifiActiveTest() {
        ActivityScenario.launch(YoutubeActivity::class.java)
        Thread.sleep(120000)
    }

}

