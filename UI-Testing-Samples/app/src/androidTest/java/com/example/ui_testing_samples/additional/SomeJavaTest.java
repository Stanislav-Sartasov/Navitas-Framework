package com.example.ui_testing_samples.additional;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ui_testing_samples.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SomeJavaTest {

    @Test
    public void mainActivity_start() {
        ActivityScenario.launch(MainActivity.class);
    }
}
