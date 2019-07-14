package com.example.trialapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private lateinit var refresh: Runnable

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewBatteryStatus = findViewById<TextView>(R.id.textViewBatteryStatus)
        val viewBatteryPlugged = findViewById<TextView>(R.id.textViewBatteryPlugged)
        val viewBatteryLevel = findViewById<TextView>(R.id.textViewBatteryLevel)
        val viewBatteryVoltage = findViewById<TextView>(R.id.textViewBatteryVoltage)
        val viewBatteryAmperage = findViewById<TextView>(R.id.textViewBatteryAmperage)
        val viewBatteryTemperature = findViewById<TextView>(R.id.textViewBatteryTemperature)

        this.handler = Handler()

        this.refresh = Runnable {

            val batteryInfo: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                this.registerReceiver(null, ifilter)
            }

            val batteryStatus: Int? = batteryInfo?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            viewBatteryStatus.text = when (batteryStatus) {
                2 -> "CHARGING"
                3 -> "DISCHARGING"
                4 -> "NOT CHARGING"
                5 -> "FULL"
                else -> "UNKNOWN"
            }

            val batteryPlugged: Int? = batteryInfo?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            viewBatteryPlugged.text = when (batteryPlugged) {
                1 -> "AC"
                2 -> "USB"
                4 -> "WIRELESS"
                else -> "NOT PLUGGED"
            }

            val batteryLevel: Int? = batteryInfo?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            viewBatteryLevel.text = batteryLevel.toString() + " %"

            val batteryVoltage: Int? = batteryInfo?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            viewBatteryVoltage.text = batteryVoltage?.div(1e3).toString() + " V"

            val batteryAmperage: Int? =
                (getSystemService(Context.BATTERY_SERVICE) as BatteryManager)
                    .getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            viewBatteryAmperage.text = batteryAmperage?.div(1e3).toString() + " mA"

            val batteryTemperature: Int? = batteryInfo?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            viewBatteryTemperature.text = batteryTemperature?.div(10).toString() + " °C"

            handler.postDelayed(refresh, 500)

        }

        handler.post(refresh)

    }

}

