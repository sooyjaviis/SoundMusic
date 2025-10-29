// Archivo: app/build.gradle.kts

plugins {
    // ESTO DEBE ESTAR UNA SOLA VEZ:
    id("com.android.application")

    id("org.jetbrains.kotlin.android")

    // Plugins necesarios para Firebase y Kotlin
    id("com.google.gms.google-services") // Asegura que este esté aquí
}

android {
    namespace = "com.example.reproductormusic" // Verifica el nombre de tu paquete
    compileSdk = 34 // O la versión que estés usando

    defaultConfig {
        applicationId = "com.example.reproductormusic"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        // 🔥 CORRECCIÓN: Usar Java 11 (la versión recomendada actualmente)
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        // 🔥 CORRECCIÓN: Actualizar el target JVM para coincidir con Java 11
        jvmTarget = "11"
    }

    // Habilitar ViewBinding para el acceso a las vistas
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX y Kotlin
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // 🔥 1. IMPLEMENTAR EL BOM (Administrador de Versiones de Firebase)
    // Esto establece todas las versiones de las librerías de Firebase de forma compatible.
    // Usamos una versión reciente, como la 32.7.0.
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // 2. Dependencias de Firebase y Google Sign-In
    // Ahora, implementas firebase-auth-ktx SIN número de versión, ya que el BOM la proporciona.
    implementation("com.google.firebase:firebase-auth-ktx") // Para Firebase Auth

    // Esta dependencia de Google Play Services SÍ NECESITA versión, ya que no es de Firebase.
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Pruebas (déjalas si las necesitas)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
