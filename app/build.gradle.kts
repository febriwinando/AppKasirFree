plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "tech.id.kasirapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "tech.id.kasirapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity)
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.auth.kt)
    implementation(libs.constraintlayout)
    implementation(libs.converter.gson)
    implementation(libs.firebase.bom)
    implementation(libs.firebase.messaging)
    implementation(libs.logging.interceptor)
    implementation(libs.material)
    implementation(libs.postgrest.kt)
    implementation(libs.retrofit)
    annotationProcessor(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.storage.kt)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}