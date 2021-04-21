package com.example.ui_testing_samples

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanSettings
import android.content.ContentValues
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BluetoothTestClass {
    lateinit var bluetoothManager: BluetoothManager

    @Test
    fun startBluetoothActivity() {
        ActivityScenario.launch(MainActivity::class.java)
        for (i in 1..10) {
            startScan()
        }
    }

    private fun startScan() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val scanner = adapter.bluetoothLeScanner
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0L)
            .build()
        if (scanner != null) {
            scanner.startScan(null, scanSettings,  null);
            Log.d(ContentValues.TAG, "scan started")
        } else {
            Log.e(ContentValues.TAG, "could not get scanner object")
        }
    }
}