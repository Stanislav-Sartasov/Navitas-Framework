## Here is a simple app and several tests (Espresso only).

### ! All launches are carried out from the root project directory.

### -- this simply assembles apks with bytecode instrumentation and installs it on the device
`gradlew profileBuild -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk -Papk_path=app/build/outputs/apk/debug/app-debug.apk` 

### -- this runs selected [test_paths] in [test_apk_path]
`gradlew runTests -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk  -Ptest_paths=NavigationTest,AnotherTest` 

### -- full-cycle task, invocating assembling, installing, running tests, loading logs and parsing it
`gradlew rawProfile -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk -Papk_path=app/build/outputs/apk/debug/app-debug.apk -Ptest_paths=NavigationTest,AnotherTest` 

### If any problems occure
 - `gradle wrapper --gradle-version=5.1.1` in case of unsupported gradle version
 - Linux isn't supported yet as invocated commands are designed for Windows (will be fixed soon)

CPU freqs and methods trace logs can be found in app/profileOutput.
