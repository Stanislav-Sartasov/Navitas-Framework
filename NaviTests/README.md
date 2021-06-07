### Android Studio sample project


#### Here are samples of NaviProf plugin's tasks calls

####  \>profileBuild

`./gradlew :app:profileBuild`

####  \>runTests

`./gradlew :app:runTests -Pgranularity=class -Ptest_paths=NavigationTest,AnotherTest`

`./gradlew :app:runTests -Ptest_paths=NavigationTest,AnotherTest`

`./gradlew :app:runTests -Pgranularity=methods -Ptest_paths=NavigationTest#mainActivity_to_childActivity_navigation:mainActivity_to_childActivity_andBack_navigation,AnotherTest#mainActivity_start`

####  \>runCustomTests

`./gradlew :app:runCustomTests -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk  -Ptest_paths=NavigationTest,AnotherTest`

####  \>defaultProfile

`./gradlew :app:defaultProfile -Pgranularity=class -Ptest_paths=NavigationTest,AnotherTest`

`./gradlew :app:defaultProfile -Pgranularity=methods -Ptest_paths=NavigationTest#mainActivity_to_childActivity_navigation:mainActivity_to_childActivity_andBack_navigation,AnotherTest#mainActivity_start`

####  \>customProfile

`./gradlew :app:customProfile -Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk -Papk_path=app/build/outputs/apk/debug/app-debug.apk -Ptest_paths=NavigationTest,AnotherTest`
