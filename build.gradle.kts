// Archivo: build.gradle.kts (Nivel Proyecto/Root)

plugins {
    // 1. CORRECCIÓN CLAVE: Usar 'com.android.library' en el root Gradle (o la nueva sintaxis si estás en una versión más reciente)
    // Para simplificar, usamos la sintaxis estándar que funciona
    id("com.android.application") version "8.3.0" apply false

    // 2. Mantenemos las versiones actualizadas para corregir el error SDK XML version 4
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false

    // 3. Plugin de Google Services para Firebase
    id("com.google.gms.google-services") version "4.4.0" apply false
}