#####Here is a simple app and several tests(Espresso & monkeyrunner).

All launches are carried out from the root project directory.

espressoRunner start example:
./gradlew espressoRunner -Papp_package=com.example.ui_testing_samples -Papk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk -Ptest_apk_path=app/build/outputs/apk/debug/app-debug.apk -Ptest_path=NavigationTest

monkeyRunner start example:
./gradlew monkeyRunner -Papk_path=nothing -Pscript_path=scripts/simple.py