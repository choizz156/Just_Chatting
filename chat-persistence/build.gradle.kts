plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.4")
    }
}

dependencies {
    implementation(project(":chat-core"))

    //security password
    implementation("org.springframework.security:spring-security-crypto")

    //redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    //embedded redis
    implementation("com.github.codemonstur:embedded-redis:1.4.3")
    implementation("org.springframework.boot:spring-boot-starter-websocket")


    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    //mongo db
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    implementation("net.coobird:thumbnailator:0.4.14")

    // embeded mongoDB
    implementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.21.0")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring3x:4.21.0")
    testImplementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("org.apache.commons:commons-lang3:3.18.0")
}
