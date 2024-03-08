pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val springDependencyManagementPluginVersion: String by settings
    val protobufPluginVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementPluginVersion
        kotlin("plugin.spring") version kotlinVersion
        id("com.google.protobuf") version protobufPluginVersion
    }
}
rootProject.name = "local-grpc-kt"
