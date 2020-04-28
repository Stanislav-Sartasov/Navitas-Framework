# Here is an example app and several tests (Espresso only).

### ! All launches are carried out from the app project directory.

##  >profileBuild
### simply assembles apks with bytecode instrumentation and installs it on the device
`gradlew profileBuild`

##  >runTests
### runs selected *test_classes* in default *test_apk_path*
 `gradlew runTests -Pgranularity=class -Ptest_paths=NavigationTest,AnotherTest`

 `gradlew runTests -Ptest_paths=NavigationTest,AnotherTest`

-> those commands are equal
### if parameter `granularity` is set to `class`, all methods of each listed class will be executed and logs will be divided by those classes
 `gradlew runTests -Pgranularity=methods -Ptest_paths=NavigationTest#mainActivity_to_childActivity_navigation:mainActivity_to_childActivity_andBack_navigation,AnotherTest#mainActivity_start`
### if parameter `granularity` is set to `methods`, selected methods of classes will be executed and logs will be divided by classes and methods
### be aware that '#' is expected after *test_class_name*. *methodName*s are split by ':'
### parameter `granularity` is set by default to `class` and is used in each below-mentioned task

##  >runCustomTests
### essentially implements same functionality as *>runTests* but accepts `test_apk_path` as parameter
`gradlew runCustomTests -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk  -Ptest_paths=NavigationTest,AnotherTest`

##  >defaultProfile
###  is full-cycle task, involving assembling, installing, running tests, loading logs and parsing it.
###  apk and test_apk are generating from `src`. It only needs `test_paths` to be passed
`gradlew defaultProfile -Pgranularity=class -Ptest_paths=NavigationTest,AnotherTest`

 `gradlew defaultProfile -Pgranularity=methods -Ptest_paths=NavigationTest#mainActivity_to_childActivity_navigation:mainActivity_to_childActivity_andBack_navigation,AnotherTest#mainActivity_start`

##  >customProfile
###  runs tests, loads logs and parses it.
###  it needs `apk_path`, `test_apk_path`, `test_paths` to be passed
`gradlew customProfile -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk -Papk_path=app/build/outputs/apk/debug/app-debug.apk -Ptest_paths=NavigationTest,AnotherTest`

### Logs and JSON with profile info can be found in {appProject}/profileOutput