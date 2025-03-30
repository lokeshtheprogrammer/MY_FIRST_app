plugins {
    id("androidx.navigation.safeargs.kotlin") version "2.7.5" apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}

buildscript {
    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(0, "seconds")
        }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")  // Updated version
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.9.22-1.0.17")  // Updated version
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
