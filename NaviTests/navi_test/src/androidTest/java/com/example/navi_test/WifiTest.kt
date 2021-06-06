package com.example.navi_test

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WifiTest {

    @Test
    fun startWifiActivity() {
        ActivityScenario.launch(YoutubeActivity::class.java)
        Thread.sleep(30000)
    }
}