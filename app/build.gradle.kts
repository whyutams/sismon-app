plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.orion"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.orion"
        minSdk = 27
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Custom
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("org.apache.poi:poi:5.2.3")
    implementation ("org.apache.poi:poi-ooxml:5.2.3")
    // firebase
    implementation(platform("com.google.firebase:firebase-bom:35.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

}