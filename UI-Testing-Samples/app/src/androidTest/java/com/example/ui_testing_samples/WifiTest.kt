package com.example.ui_testing_samples

import android.net.wifi.WifiManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileOutputStream
import java.net.InetAddress
import java.net.Socket
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

@RunWith(AndroidJUnit4::class)
class WifiTest {
    lateinit var wifiManager: WifiManager

    @Test
    fun startWifiActivity() {
        ActivityScenario.launch(MainActivity::class.java)

        for (i in 1..10) {
            pingAndLoad(URL("ya.ru"))
        }
    }

    private fun pingAndLoad (url: URL) {
        try {
            val hostAddress: String = InetAddress.getByName(url.host).hostAddress
            val socket = Socket(hostAddress, url.port)
            Thread.sleep(100)
            socket.close()
        } catch (ex: Exception) {
            print(ex.message)
        }

        val rbc: ReadableByteChannel = Channels.newChannel(url.openStream())
        val fd = FileOutputStream("witiTest.html")
        fd.channel.transferFrom(rbc, 0, Long.MAX_VALUE);
        rbc.close()
    }
}