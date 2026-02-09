plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ownboard.app"
    compileSdk = 33

    buildFeatures {
        viewBinding = true
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    defaultConfig {
        applicationId = "com.ownboard.app"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
    implementation("androidx.viewpager2:viewpager2:1.0.0")


    // 2. لعرض شبكة الإيموجي (Grid) بسرعة عالية
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // 3. للمعالجة في الخلفية (تحميل JSON وقاعدة البيانات)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // 4. لربط العمليات بدورة حياة التطبيق
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
}