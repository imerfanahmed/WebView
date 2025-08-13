plugins {
    alias(libs.plugins.android.application)
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("/home/erfan/Downloads/EdueasyTeacher-main/edueasy.jks")
            storePassword = "password"
            keyAlias = "edueasy"
            keyPassword = "password"
        }
    }
    namespace = "com.dataenvelope.edueasy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dataenvelope.edueasy"
        minSdk = 28
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    buildToolsVersion = "36.0.0"
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}