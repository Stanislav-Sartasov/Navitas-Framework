// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
        /*
        //It was used for downloading from Github-packages
        maven("https://maven.pkg.github.com/Stanislav-Sartasov/Navitas-Framework/") {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Stanislav-Sartasov/Navitas-Framework/")
            credentials {
                username = (project.findProperty("gpr.user") ?: System.getenv("USERNAME")).toString()
                password = (project.findProperty("gpr.key") ?: System.getenv("TOKEN")).toString()
            }
        }*/
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.41")
        classpath("com.github.stanislav-sartasov:NaviProf:1.16")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
