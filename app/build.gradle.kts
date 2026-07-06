plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // ...plugins del módulo (Android, Kotlin, Compose)...
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.gastospersonales"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.gastospersonales"
        minSdk = 29
        targetSdk = 36
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
        compose = true
    }
}

dependencies {
    // ── Base del proyecto (plantilla Compose + Material 3) ──
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.compose.material.icons.extended) // íconos de categoría

    // ── Sprint 1 · Room (base de datos) ──
    implementation(libs.androidx.room.runtime)  // núcleo de Room
    implementation(libs.androidx.room.ktx)      // soporte Flow/Coroutines
    ksp(libs.androidx.room.compiler)            // genera el código (KSP)

    // ── Sprint 2 · MVVM y reactividad ──
    implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel en Compose
    implementation(libs.androidx.lifecycle.runtime.compose)   // collectAsStateWithLifecycle
    implementation(libs.kotlinx.coroutines.android) // Coroutines

    // ── Sprint 3 · Navegación ──
    implementation(libs.androidx.navigation.compose) // NavHost y rutas

    // ── Sprint 4 · Preferencias, imágenes ──
    implementation(libs.androidx.datastore.preferences) // tema, moneda, formato
    implementation(libs.coil.compose)                  // cargar la foto del recibo
}