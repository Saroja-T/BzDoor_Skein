import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.busydoor.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.busydoor.app"
        minSdk = 24
        targetSdk = 33
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("com.github.Adilhusen:circle-progress-ad-android-:1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    /*** ---app require libraries--- ***/
    /*** google message service ***/
    implementation ("com.google.android.gms:play-services-vision:20.1.3")
    implementation ("com.google.android.gms:play-services-vision-common:19.1.3")
    /*** gson  ***/
    implementation ("com.google.code.gson:gson:2.8.6")
    /*** retrofit ***/
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    /***  Import the BoM for the Firebase platform ***/
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    /*** beacon library ***/
    implementation("org.altbeacon:android-beacon-library:2.19.6")
    /*** support animations widget ***/
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.19")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.intuit.sdp:sdp-android:1.1.0")

    /*** support For Kotlin  widget ***/
    implementation ("androidx.work:work-runtime-ktx:2.7.1")
    /*** Image loading ***/
    implementation ("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.11.0")
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    /*** SmoothBottomBar  library ***/
//    implementation ("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")
    /*** MPchart  library ***/
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    /*** switch_button  library ***/
    implementation ("com.github.GwonHyeok:StickySwitch:0.0.16")

    implementation ("com.getbase:floatingactionbutton:1.10.1")

    /*** firebase  library ***/
//    implementation ("com.google.firebase:firebase-analytics-ktx")
    /*** Firebase notification ***/
//    implementation ("com.google.firebase:firebase-messaging:23.2.1")
    // Add the dependency for the App Check library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation ("com.google.firebase:firebase-appcheck-playintegrity")
    implementation ("devs.mulham.horizontalcalendar:horizontalcalendar:1.3.4")
    // Declare the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
//    implementation ("com.google.firebase:firebase-auth-ktx")

    // Declare the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
//    implementation ("com.google.firebase:firebase-crashlytics")
//    implementation ("com.google.firebase:firebase-analytics")
    implementation ("com.github.gastricspark:scrolldatepicker:0.0.1")
    implementation ("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")
}