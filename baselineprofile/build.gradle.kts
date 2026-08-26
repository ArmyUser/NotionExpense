plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
    id("androidx.baselineprofile")
}

android {
    namespace = "it.speses22.baselineprofile"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        // La raccolta del profilo richiede API 28+.
        minSdk = 28
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // L'unico dispositivo qui e' un emulatore: senza questo il benchmark
        // rifiuta di girare. I numeri vanno letti come indicativi.
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] =
            "EMULATOR,UNLOCKED,LOW-BATTERY"
    }

    targetProjectPath = ":app"

    // Il modulo si auto-istruisce: nessun APK di test separato da installare.
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    // L'unico dispositivo disponibile qui e' un emulatore.
    useConnectedDevices = true
}

dependencies {
    implementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.4.1")
}
