plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "algo_chat"

include("chat-application",
    "chat-domain",
    "chat-persistence")
