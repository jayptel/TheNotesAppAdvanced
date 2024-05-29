plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id ("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id ("androidx.navigation.safeargs.kotlin")

}

android {
    namespace = "com.rhythm.thenotesapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rhythm.thenotesapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
    //packagingOptions.resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
   /* packagingOptions {
        resources.excludes.add("META-INF/DEPENDENCIES")
    }*/
    packaging {
        resources {
            excludes.add("META-INF/*")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        dataBinding = true
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("org.apache.poi:poi:5.2.5")
    implementation ("org.apache.poi:poi-ooxml:5.2.5")

    // ROOM
    val roomVersion = "2.6.1"
    implementation ("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation ("androidx.room:room-ktx:$roomVersion")
    // Navigation
    val navVersion = "2.7.5"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    // Life Cycle Arch
    val lifecycleVersion = "2.6.2"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    // Annotation processor
    ksp("androidx.lifecycle:lifecycle-compiler:$lifecycleVersion")

    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.api-client:google-api-client-android:1.33.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev612-1.25.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.33.0")
   /* implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.sheets)
    implementation(libs.google.api.services.drive)
    implementation(libs.google.oauth.client.jetty)
    implementation(libs.play.services.auth)*/
    //implementation("com.google.api-client:google-api-client-android:1.33.0")
   //implementation("com.google.apis:google-api-services-sheets:v4-rev612-1.25.0")
    //implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0")
    //implementation("com.google.oauth-client:google-oauth-client-jetty:1.33.0")
    //implementation("com.google.android.gms:play-services-auth:20.2.0")
   //implementation("com.google.api-client:google-api-client-gson:1.33.0")
   //implementation("com.google.api-client:google-api-client-extensions-android:1.33.2")
    // Jackson for JSON parsing
    //implementation("com.fasterxml.jackson.core:jackson-core:2.13.3")
    //implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    //implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.3")
    //implementation("com.google.http-client:google-http-client-jackson2:1.40.0")
    implementation("com.android.support:multidex:1.0.3")
}