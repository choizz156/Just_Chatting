plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.4")
    }
}

dependencies {
    implementation(project(":chat-core"))
    implementation(project(":chat-persistence"))
    implementation(project(":chat-auth"))
    implementation(project(":chat-api"))
    implementation(project(":chat-websocket"))


    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    //mongo db
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    implementation("org.apache.commons:commons-lang3:3.18.0")

}


