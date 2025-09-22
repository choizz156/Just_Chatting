plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.4")
    }
}

dependencies {
    implementation(project(":chat-core"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.github.codemonstur:embedded-redis:1.4.3")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    implementation("org.springframework.boot:spring-boot-starter-websocket")

    implementation("org.springframework.security:spring-security-crypto")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")


    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:1.1.15")

}