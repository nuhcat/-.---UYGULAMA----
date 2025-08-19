buildscript {
    dependencies {
        // Android Gradle plugin ve Firebase Google Services plugin
        classpath ("com.android.tools.build:gradle:8.3.0")  // Veya kullandığınız sürüm
        classpath ("com.google.gms:google-services:4.4.1")  // Firebase plugin'i
    }
}



// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}


