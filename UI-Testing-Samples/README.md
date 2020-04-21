## Here is an example app and several tests (Espresso only).

### ! All launches are carried out from the app project directory.

###  >profileBuild
#### simply assembles apks with bytecode instrumentation and installs it on the device
`gradlew profileBuild`

###  >runTests
#### runs selected [test_paths] in default [test_apk_path]
`gradlew runTests -Ptest_paths=NavigationTest,AnotherTest`

###  >runCustomTests
#### runs selected [test_paths] in [test_apk_path]
`gradlew runCustomTests -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk  -Ptest_paths=NavigationTest,AnotherTest`

###  >defaultProfile
####  is full-cycle task, invocating assembling, installing, running tests, loading logs and parsing it.
####  apk and test_apk are generating from `src`. Only needs [test_paths] to be passed
`gradlew defaultProfile -Ptest_paths=NavigationTest,AnotherTest`

###  >customProfile
####  runs tests, loads logs and parses it.
####  it needs [apk_path], [test_apk_path] and [test_path] to be passed
`gradlew customProfile -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk -Papk_path=app/build/outputs/apk/debug/app-debug.apk -Ptest_paths=NavigationTest,AnotherTest`

#### Logs for each method and JSON with all profile info can be found in {app}/profileOutput