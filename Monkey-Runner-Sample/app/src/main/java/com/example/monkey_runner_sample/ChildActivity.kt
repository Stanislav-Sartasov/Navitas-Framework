package com.example.monkey_runner_sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ChildActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child)

        findViewById<Button>(R.id.child_button).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.child_title).text = intent.getStringExtra(BUTTON_NAME_EXTRA)
    }

    companion object {
        const val BUTTON_NAME_EXTRA = "button_name"
    }
}
