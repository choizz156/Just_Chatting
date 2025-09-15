plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "just_chat"

include("chat-application",
    "chat-core",
    "chat-persistence",
    "chat-api", "chat-auth")
