plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("java-library")
    id("io.spring.dependency-management")
}



dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.4")
    }
}

dependencies {
    implementation(project(":chat-persistence"))
    implementation(project(":chat-core"))

    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-test")

    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}