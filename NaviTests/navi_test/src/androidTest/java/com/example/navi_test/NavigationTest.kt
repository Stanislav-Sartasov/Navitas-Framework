package com.example.navi_test

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @Test
    fun mainActivity_to_childActivity_navigation() {
        ActivityScenario.launch(MainActivity::class.java)

        //Move to 'Child 1' screen
        onView(withId(R.id.button_1)).perform(click())

        //Check that label of ChildActivity has "Child 1" text
        onView(withId(R.id.child_title)).check(matches(withText("Child 1")))
    }

    @Test
    fun mainActivity_to_childActivity_andBack_navigation() {
        mainActivity_to_childActivity_navigation()

        //Back to main screen
        onView(withId(R.id.child_button)).perform(click())

        //Check that current activity is MainActivity
        check(getCurrentActivity() is MainActivity)
    }

    private fun getCurrentActivity(): Activity? {
        var currentActivity: Activity? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val resumedActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            currentActivity = resumedActivity.iterator().next()
        }

        return currentActivity
    }
}