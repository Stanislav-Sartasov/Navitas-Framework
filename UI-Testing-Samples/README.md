## Here is a simple app and several tests (Espresso only).

### ! All launches are carried out from the root project directory.

* full profiling cycle with EspressoRunner:\
gradlew profile -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk -Papk_path=app/build/outputs/apk/debug/app-debug.apk -Ptest_paths=NavigationTest,AnotherTest

Battery stats and methods trace logs can be found in app/profileOutput.