plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "cafe.serenity.tbilisiweather"
    compileSdk = 34

    defaultConfig {
        applicationId = "cafe.serenity.tbilisiweather"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation( "androidx.core:core-ktx:1.12.0" )
    implementation( "androidx.appcompat:appcompat:1.6.1" )
    implementation( "com.google.android.material:material:1.9.0" )
    implementation( "androidx.compose.ui:ui:1.5.1" )
    implementation( "androidx.compose.material:material:1.5.1" )
    implementation( "androidx.compose.ui:ui-tooling-preview:1.5.1" )
    implementation( "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2" )
    implementation( "androidx.activity:activity-compose:1.7.2" )
    debugImplementation ("androidx.compose.ui:ui-tooling:1.5.1" )
    implementation( "io.ktor:ktor-client-core:1.6.4" )
    implementation( "io.ktor:ktor-client-android:1.6.4" )
    implementation( "io.ktor:ktor-client-serialization:1.6.4" )
    implementation( "io.ktor:ktor-client-logging:1.6.4" )
    implementation( "ch.qos.logback:logback-classic:1.2.3" )

    implementation( "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0" )

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.1.1")
}