import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Gradle/AGP itself needs JDK 17 to run - this makes that a fact Gradle enforces (provisioning
// a matching JDK via its toolchain resolver if none is on the machine) instead of a line in
// CONTRIBUTING.md nobody's build actually checks. Unrelated to compileOptions/kotlinOptions
// below, which target Java 11 *bytecode* for the app itself - the JDK that compiles it and the
// bytecode level it compiles down to are independent knobs.
kotlin {
    jvmToolchain(17)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.neojelll.diaxtracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.neojelll.diaxtracker"
        minSdk = 26
        targetSdk = 35
        // Play Store requires this to strictly increase - bump by hand alongside versionName.
        versionCode = 3
        versionName = "0.2.0-beta.1" // x-release-please-version
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        // Keeps the real applicationId to release alone: nothing built for development can
        // land on top of the real app, even by a stray installDebug.
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                logger.warn(
                    "keystore.properties not found at $keystorePropertiesFile - " +
                        "release build falling back to debug signing."
                )
                signingConfigs.getByName("debug")
            }
        }
        // A release build under its own applicationId, so it installs next to the real app
        // instead of over it: own database, prefs, files and permissions. Every on-device
        // experiment (PR builds, migrations, import/backup fixes) goes here, never onto the
        // only copy of the diary. Debug-signed - it never updates the real install, so it
        // doesn't need the release keystore. Name and icon differ via src/dev/res.
        create("dev") {
            initWith(getByName("release"))
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += "release"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil.compose)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
}
