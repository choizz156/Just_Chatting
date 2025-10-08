package com.chat.persistence.config

import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import de.flapdoodle.reverse.transitions.Start
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration

@Profile("local")
//@Configuration
class EmbeddedMongoConfig : AbstractMongoClientConfiguration(){

    private var runningMongod: TransitionWalker.ReachedState<RunningMongodProcess>? = null

    @Bean
    fun embeddedMongoServer(): RunningMongodProcess {
        runningMongod = Mongod.instance()
            .withNet(
                Start.to(Net::class.java)
                    .initializedWith(
                        Net.builder()
                            .port(27017)
                            .bindIp("localhost")
                            .isIpv6(false)
                            .build()
                    )
            )
            .start(Version.Main.V4_4)

        return runningMongod!!.current()
    }

    override fun getDatabaseName(): String = "testdb"

    override fun mongoClientSettings(): com.mongodb.MongoClientSettings {
        val process = embeddedMongoServer()
        return com.mongodb.MongoClientSettings.builder()
            .applyConnectionString(
                com.mongodb.ConnectionString("mongodb://localhost:${process.serverAddress.port}/testdb")
            )
            .build()
    }

    @PreDestroy
    fun cleanup() {
        runningMongod?.close()
    }

}