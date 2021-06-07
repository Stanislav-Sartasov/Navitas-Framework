package com.example.navi_test

fun doInBackground(): Long {
    var x = 0L
    for (i in 0..100000) {
        x += i * i
    }
    return x
}