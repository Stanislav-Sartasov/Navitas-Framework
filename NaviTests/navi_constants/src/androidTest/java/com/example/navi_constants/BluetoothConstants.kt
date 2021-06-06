package com.example.navi_constants

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BluetoothConstants {

    @Test
    fun bluetoothOn() {
        ActivityScenario.launch(MainActivity::class.java)
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()
        }
        Thread.sleep(30000)
    }

    @Test
    fun bluetoothScan() {
        ActivityScenario.launch(MainActivity::class.java)
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()
        }
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < 30000)
        {
            startScan(scanCallbackWithoutConnect)
        }
    }

    @Test
    fun bluetoothActive() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()
        }
        if (mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) != BluetoothHeadset.STATE_CONNECTED)
        {
            Log.d("Bluetooth active warning", "Bluetooth device not connected")
        }
        ActivityScenario.launch(MusicActivity::class.java)
        Thread.sleep(30000)
    }

    private fun startScan(scanCallback: ScanCallback) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val scanner = adapter.bluetoothLeScanner
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0L)
            .build()

        scanner?.startScan(null, scanSettings, scanCallback)
    }

    private val scanCallbackWithoutConnect: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // ...do whatever you want with this found device
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            // Ignore for now
        }

        override fun onScanFailed(errorCode: Int) {
            // Ignore for now
        }
    }

    //TODO: find a way to connect to bluetooth device via test
//    private val scanCallbackWithConnect: ScanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            val device: BluetoothDevice = result.device
//            val gatt = device.connectGatt(
//                MainActivity::getContext as Context, false,
//                null, TRANSPORT_LE
//            )
//
//            // ...do whatever you want with this found device
//        }
//
//        override fun onBatchScanResults(results: List<ScanResult?>?) {
//            // Ignore for now
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            // Ignore for now
//        }
//    }
}