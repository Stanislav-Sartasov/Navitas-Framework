// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        maven("https://maven.pkg.github.com/Stanislav-Sartasov/Navitas-Framework/") {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Stanislav-Sartasov/Navitas-Framework/")
            credentials {
                username = (project.findProperty("gpr.user") ?: System.getenv("USERNAME")).toString()
                password = (project.findProperty("gpr.key") ?: System.getenv("TOKEN")).toString()
            }
        }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.6.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
        classpath("com.Navitas:NaviProf:1.11")
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
