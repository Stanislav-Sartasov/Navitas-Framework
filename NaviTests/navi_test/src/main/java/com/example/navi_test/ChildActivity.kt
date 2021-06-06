package com.example.navi_test

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ChildActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child)

        findViewById<Button>(R.id.child_button).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.child_title).text = intent.getStringExtra(BUTTON_NAME_EXTRA)
    }

    override fun onStart() {
        super.onStart()

        executeSomething()
        executeSomething()
    }
       
    override fun onStop() {
        super.onStop()
        
        rec(10)
    }

    private fun executeSomething(): Long {
        var x = 0L
        for (i in 0..1000) {
            for (j in 0..1000) {
                x += (i + j)
            }
        }
        return x
    }
    
    private fun rec(value: Int) {
        if (value > 0) 
            rec(value - 1)
    }

    companion object {
        const val BUTTON_NAME_EXTRA = "button_name"
    }
}
