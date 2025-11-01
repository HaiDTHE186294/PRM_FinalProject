import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.lkms"
    compileSdk = 36

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }

    defaultConfig {
        applicationId = "com.lkms"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            type = "String",
            name = "SUPABASE_URL",
            value = "\"${localProperties.getProperty("SUPABASE_URL")}\""
        )

        buildConfigField(
            type = "String",
            name = "SUPABASE_ANON_KEY",
            value = "\"${localProperties.getProperty("SUPABASE_ANON_KEY")}\""
        )

        buildConfigField(
            type = "String",
            name = "JWT_SECRET",
            value = "\"${localProperties.getProperty("JWT_SECRET", "DEFAULT_SECRET_KEY")}\""
        )
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
        dataBinding = true
        buildConfig = true
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }


}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.3")
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    // SUPABASE - JAN TENNERT (for Kotlin code)
    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")


    // GSON (Dependency for Harium Supabase)
    implementation("com.google.code.gson:gson:2.10.1")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    // Các dependency khác của bạn
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    //JWT
    implementation("com.auth0:java-jwt:4.5.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    //Bcrypt
    implementation("org.mindrot:jbcrypt:0.4")
    
    implementation("com.applandeo:material-calendar-view:1.9.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    //PDF Viewer
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.1")
}


// Tác vụ tùy chỉnh để chạy Supabase test runner từ console
val runTest by tasks.registering(JavaExec::class) {
    group = "Verification"
    description = "Runs the Supabase connection test from the console"

    // Chỉ định class chứa hàm main() để thực thi
    mainClass.set("com.lkms.tools.SupabaseTestRunnerKt")
    // Cho phép tác vụ nhận input từ console
    standardInput = System.`in`
}
