### Android Studio sample project


#### Here are samples of NaviProf plugin's tasks calls

####  \>profileBuild

`./gradlew profileBuild`

####  \>runTests

`./gradlew runTests -Pgranularity=class -Ptest_paths=NavigationTest,AnotherTest`

`./gradlew runTests -Ptest_paths=NavigationTest,AnotherTest`

`./gradlew runTests -Pgranularity=methods -Ptest_paths=NavigationTest#mainActivity_to_childActivity_navigation:mainActivity_to_childActivity_andBack_navigation,AnotherTest#mainActivity_start`

####  \>runCustomTests

`./gradlew runCustomTests -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk  -Ptest_paths=NavigationTest,AnotherTest`

####  \>defaultProfile

`./gradlew defaultProfile -Pgranularity=class -Ptest_paths=NavigationTest,AnotherTest`

`./gradlew defaultProfile -Pgranularity=methods -Ptest_paths=NavigationTest#mainActivity_to_childActivity_navigation:mainActivity_to_childActivity_andBack_navigation,AnotherTest#mainActivity_start`

####  \>customProfile

`./gradlew customProfile -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk -Papk_path=app/build/outputs/apk/debug/app-debug.apk -Ptest_paths=NavigationTest,AnotherTest`
