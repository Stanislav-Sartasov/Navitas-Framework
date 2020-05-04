## NaviProf plugin
### Implementation of power-profiling Gradle plugin
NaviProf consist of a set of tasks (profileBuild, runTests, runCustomTests, defaultProfile, customProfile) that provide functionality of:
 1. assembling *app_apk* and *test_apk* with instrumenting bytecode that allows to generate logs of cpu cores frequencies at start and end of method's invocation;
 2. installing *app_apk* and *test_apk* on device, clearing device's logs and running selected test methods;
 3. unloading of log files and transforming it to json with information about energy consumption (CPU activity) that can be further handled or visually presented. 
#### actual version is 1.17
#### latest compatible version of com.android.tools.build:gradle is 3.4.2
Can be downloaded from 

 - Github packages: deprecated;    
 - MavenCentral: add `classpath("com.github.stanislav-sartasov:NaviProf:${actual_version}")` to your project's build.gradle dependencies;    
 
It is recommended to apply plugin at the bottom of the file 

## API:
####  \>profileBuild
Assembles **app-debug.apk** and **app-debug-androidTest.apk** with bytecode instrumentation and installs it on the device
 
`./gradlew :{appModule}:profileBuild`

#### \>runTests
Runs all the methods of selected test_classes in default **test_apk_path**. 
 - **test_paths** accepts test classes names (split with ',') to be run 
 - depending on **granularity** you should either pass ClassNames or ClassNames with MethodNames
 - if parameter **granularity** is set to `class` or not specified, data files will be divided into classes
 - if parameter **granularity** is set to `methods`, data files will be divided into test methods; pass ClassName + # + methods separated by ':' to **test_paths**


 `./gradlew :{appModule}:runTests -Pgranularity=class -Ptest_paths={TestClass1Name},{TestClass2Name}`

 `./gradlew :{appModule}:runTests -Ptest_paths={TestClass1Name},{TestClass2Name}`

-> those two commands are equal

 `./gradlew :{appModule}:runTests -Pgranularity=methods -Ptest_paths={TestClass1Name}#{TestMethod1Name}:{TestMethod2Name},{TestClass2Name}#{TestMethod3Name}:{TestMethod4Name}`
 
###  \>runCustomTests
Essentially implements same functionality as **\>runTests** but requires **test_apk_path** as parameter 
 - **test_apk_path** accepts path to test apk which will be used for running tests

`./gradlew :{appModule}:runCustomTests -Ptest_apk_path={PathToTestApk}  -Ptest_paths={TestClass1Name},{TestClass2Name}`

`./gradlew :{appModule}:runCustomTests -Pgranularity=methods -Ptest_apk_path={PathToTestApk}  -Ptest_paths={TestClass1Name}#{TestMethod1Name}:{TestMethod2Name},{TestClass2Name}#{TestMethod3Name}:{TestMethod4Name}`
                                                                                               

###  \>defaultProfile
Is full-cycle task, involving assembling, installing, running tests, loading logs and parsing it.

 - apk and test_apk are generating from `src`
 - it only requires **test_paths** parameter

`./gradlew :{appModule}:defaultProfile -Ptest_paths={TestClass1Name},{TestClass2Name}`

 `./gradlew :{appModule}:defaultProfile -Pgranularity=methods -Ptest_paths={TestClass1Name}#{TestMethod1Name}:{TestMethod2Name},{TestClass2Name}#{TestMethod3Name}:{TestMethod4Name}`

###  \>customProfile
Unlike **\>defaultProfile** it doesn't assemble or install apks.

 - it needs **apk_path**, **test_apk_path**, **test_paths** to be passed
 - it supposes that apk and test_apk passed to **apk_path** and **test_apk_path** are already assembled and installed

`./gradlew :{appModule}:customProfile -Ptest_apk_path={PathToTestApk} -Papk_path={PathToAppApk} -Ptest_paths={TestClass1Name},{TestClass2Name}`

`./gradlew :{appModule}:customProfile -Pgranularity=methods -Ptest_apk_path={PathToTestApk} -Papk_path={PathToAppApk} -Ptest_paths={TestClass1Name}#{TestMethod1Name}:{TestMethod2Name},{TestClass2Name}#{TestMethod3Name}:{TestMethod4Name}`


#### Logs and JSON with profile info can be found in {appModule}/profileOutput.
    
    
  
    
## Instruction for publishing plugin to mavenCentral:   
    1) `gpg --gen-key` (remember password) 
        -> `gpg --list-keys --keyid-format short` for KeyId 
	    -> `gpg --export-secret-keys -o secret-keys.gpg` (remember destination)    
    2) `gpg --keyserver hkp://keyserver.ubuntu.com --send-keys {KeyId}` 
        -> `gpg --keyserver hkp://pool.sks-keyservers.net --send-keys {KeyId}` 
	    -> `gpg --keyserver hkp://keys.openpgp.org --send-keys {KeyId}`
    3) Sign up for Jira https://issues.sonatype.org/secure/Signup!default.jspa and then ask for a permission
    4) Place to ${home}/.gradle/gradle.properties
    `nexusUsername=YOUR_SONATYPE_USER_NAME  
     nexusPassword=YOUR_SONATYPE_USER_PASSWORD  
  
     signing.keyId=KEY_ID  
     signing.password=KEY_PASSWORD  
     signing.secretKeyRingFile=/PATH/TO/SECRET/RING/FILE`
    5) `./gradlew uploadArchives` 
        -> `./gradlew closeAndReleaseRepository`
    6) Profit

    Instruction for publishing to mavenLocal:
    1) `./gradlew publishToMavenLocal`
    2) in project's build.gradle replace mavenCentral repository with mavenLocal
