**Implementation of power-profiling Gradle plugin** actual version is 1.16;    
Can be downloaded from Github packages: deprecated;    
MavenCentral: add `classpath("com.github.stanislav-sartasov:NaviProf:${plugin_version}")` to your build.gradle dependencies and apply "NaviProf" plugin;    
To see plugin's API check UI-Testing-Samples.    
    
    Instruction for publishing plugin to mavenCentral:   
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
