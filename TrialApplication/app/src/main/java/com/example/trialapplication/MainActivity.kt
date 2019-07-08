package com.example.trialapplication

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
                ifilter -> this.registerReceiver(null, ifilter)
        }

        val batteryPct: Int? = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val vText = findViewById<TextView>(R.id.act_text)

        val text = "Current battery level: " + batteryPct.toString() + " percent"
        vText.setText(text)
    }

}
