package com.example.ui_testing_samples

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private val onButtonClickListener = View.OnClickListener {
        val intent = Intent(this, ChildActivity::class.java)
            .putExtra(ChildActivity.BUTTON_NAME_EXTRA, (it as Button).text)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_1).setOnClickListener(onButtonClickListener)
        findViewById<Button>(R.id.button_2).setOnClickListener(onButtonClickListener)
    }
}
