import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.kapt")
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

        project.rootProject.file("open_weather_api.properties").let { file ->
            if (file.exists()) {
                Properties().let { properties ->
                    properties.load(file.inputStream())
                    properties.getProperty("OPEN_WEATHER_API_KEY").let {
                        buildConfigField("String", "OPEN_WEATHER_API_KEY", "\"$it\"")
                    }
                }
            }
        }

        buildFeatures {
            buildConfig = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
    implementation( "com.google.android.material:material:1.10.0" )
    implementation( "androidx.compose.ui:ui:1.5.3" )
    implementation( "androidx.compose.material:material:1.5.3" )
    implementation( "androidx.compose.ui:ui-tooling-preview:1.5.3" )
    implementation( "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2" )
    implementation( "androidx.activity:activity-compose:1.8.0" )
    implementation( "io.ktor:ktor-client-core:2.3.5" )
    implementation( "io.ktor:ktor-client-android:2.3.5" )
    implementation( "io.ktor:ktor-client-serialization:1.6.4" )
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")

    implementation( "ch.qos.logback:logback-classic:1.2.3" )

    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material:1.5.3")
    implementation("androidx.compose.material3:material3:1.1.2")

    //Room
    implementation("androidx.room:room-ktx:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2")
}