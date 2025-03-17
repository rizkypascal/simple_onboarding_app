import java.util.Properties

plugins {
    id("kotlin-kapt")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    val apiKey: String
    val apiSecret: String
    val baseUrl: String
    val fuelUrl: String

    apiKey = (project.findProperty("API_KEY") ?: "").toString()
    apiSecret = (project.findProperty("API_SECRET") ?: "").toString()
    baseUrl = (project.findProperty("BASE_URL") ?: "").toString()
    fuelUrl = (project.findProperty("FUEL_URL") ?: "").toString()

    namespace = "com.example.simpleonboardingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.simpleonboardingapp"
        minSdk = 26
        targetSdk = 35
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
        debug {
            buildConfigField("String", "API_KEY", "\"${apiKey}\"")
            buildConfigField("String", "API_SECRET", "\"${apiSecret}\"")
            buildConfigField("String", "BASE_URL", "\"${baseUrl}\"")
            buildConfigField("String", "FUEL_URL", "\"${fuelUrl}\"")
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
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.dotenv.vault.kotlin)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.android.gif.drawable)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.iproov)
    implementation(group = "com.iproov.android-api-client", name = "kotlin-fuel", version = "3.0.0")
    implementation(group = "com.iproov.android-api-client", name = "kotlin-common", version = "3.0.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}