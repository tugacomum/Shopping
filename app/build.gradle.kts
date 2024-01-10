plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.shopping"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.shopping"
        minSdk = 24
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
    implementation("com.cloudinary:cloudinary-android:2.5.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.1.0")
    implementation ("com.google.zxing:core:3.3.3")
    implementation("com.android.volley:volley:1.2.0")
    implementation ("androidx.core:core-splashscreen:1.1.0-alpha02")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}