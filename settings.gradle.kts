pluginManagement {
    repositories {
        google()  // 🔹 Repositorio de Google
        mavenCentral()  // 🔹 Maven Central para dependencias
        gradlePluginPortal()  // 🔹 Plugins de Gradle
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)  // 🔥 Permite usar solo los repositorios de settings
    repositories {
        google()
        mavenCentral()
    }
}

// ✅ Nombre del proyecto
rootProject.name = "MercaApp"

// ✅ Incluir módulos del proyecto
include(":app")
