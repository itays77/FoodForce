

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    val material3_version = "1.1.2"
}

android {
    namespace = "com.example.foodforceapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.foodforceapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //Firebase:
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    //Firebase Auth:
    implementation (libs.firebase.ui.auth)
    implementation(libs.firebase.database)

    //Firebase Storage:
    implementation(libs.firebase.storage)

    //GoogleMaps
    implementation(libs.play.services.maps)
    val material3_version = "1.1.2"
    implementation("androidx.compose.material3:material3:$material3_version")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.core:core:1.3.0")

}