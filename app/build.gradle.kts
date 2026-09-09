import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// La clave de Gemini se lee de local.properties, que NO va al control de versiones.
// Si no existe, queda vacia: la app compila igual y falla de forma controlada
// cuando se intente usar la IA (Tarea 3).
val localProperties = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
val geminiApiKey: String = localProperties.getProperty("GEMINI_API_KEY") ?: ""

// ID del cliente OAuth de tipo *Web* de Google Cloud. Lo necesita Credential Manager
// para identificar el login. Tambien sale de local.properties: no se escribe en el
// codigo ni se sube al repositorio. Si falta, la app avisa en pantalla (Tarea 2).
val googleWebClientId: String = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: ""

// Modelo de Gemini. Configurable porque los nombres de modelo cambian con el tiempo:
// si el que trae por defecto deja de existir, se cambia aqui sin tocar el codigo.
val geminiModelo: String = localProperties.getProperty("GEMINI_MODELO") ?: "gemini-2.5-flash"

// Datos de firma del APK que se instala en el celular. Viven en keystore.properties,
// que NO se sube al control de versiones. Si el archivo no existe, la version de
// release simplemente no se firma (util para compilar sin tener las claves a mano).
val propiedadesDeFirma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
val hayFirma = propiedadesDeFirma.getProperty("almacen") != null

android {
    namespace = "com.butterfly.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.butterfly.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
        buildConfigField("String", "GEMINI_MODELO", "\"$geminiModelo\"")
    }

    signingConfigs {
        if (hayFirma) {
            create("butterfly") {
                storeFile = rootProject.file(propiedadesDeFirma.getProperty("almacen"))
                storePassword = propiedadesDeFirma.getProperty("clave")
                keyAlias = propiedadesDeFirma.getProperty("alias")
                keyPassword = propiedadesDeFirma.getProperty("clave")
            }
        }
    }

    buildTypes {
        release {
            if (hayFirma) signingConfig = signingConfigs.getByName("butterfly")

            // La minificacion queda desactivada a proposito: puede romper en silencio
            // el codigo que usa reflexion (las librerias de Google) y este proyecto
            // todavia no tiene pruebas que lo detecten. Activarla es una decision para
            // cuando la app lleve tiempo funcionando.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// El objetivo de JVM va aqui, al nivel del script: NO dentro de android { }.
// (En android { } iba el antiguo kotlinOptions, ya obsoleto.)
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.id)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.work)

    debugImplementation(libs.androidx.ui.tooling)
}
