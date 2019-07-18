from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

def screenshot(path):
    result = device.takeSnapshot()
    result.writeToFile(directory + path,'png')

def delay(seconds):
    MonkeyRunner.sleep(seconds)

def press_with_screenshot(code, path):
    device.press(code, MonkeyDevice.DOWN_AND_UP)
    screenshot(path)

# Connects to the current device, returning a MonkeyDevice object
device = MonkeyRunner.waitForConnection()

directory = '.'
package = 'com.example.ui_testing_samples'
activity = 'com.example.ui_testing_samples.MainActivity'

# Installs the Android package. Notice that this method returns a boolean, so you can test
# to see if the installation worked.
device.installPackage(directory + '/app/build/outputs/apk/debug/app-debug.apk')
device.startActivity(component = package + '/' + activity)

delay(1)

screenshot('/screenshots/launch-state.png')

delay(1)

#Move to 'Child 1' screen
device.press('DPAD_DOWN', MonkeyDevice.DOWN_AND_UP)
press_with_screenshot('DPAD_CENTER', '/screenshots/state-1.png')

delay(1)

#Back to main screen via custom 'BACK' button
device.press('DPAD_DOWN', MonkeyDevice.DOWN_AND_UP)
press_with_screenshot('DPAD_CENTER', '/screenshots/state-2.png')

delay(1)

#Move to 'Child 2' screen
device.press('DPAD_DOWN', MonkeyDevice.DOWN_AND_UP)
press_with_screenshot('DPAD_CENTER', '/screenshots/state-3.png')

delay(1)

#Back to main screen via Back button
press_with_screenshot('KEYCODE_BACK', '/screenshots/state-4.png')

delay(1)

screenshot('/screenshots/termination-state.png')

#Terminate app
device.press('KEYCODE_HOME', MonkeyDevice.DOWN_AND_UP)
device.shell('am force-stop ' + package)