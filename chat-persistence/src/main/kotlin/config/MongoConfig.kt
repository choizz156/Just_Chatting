package com.chat.persistence.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.gridfs.GridFsTemplate

@Configuration
class MongoConfig {

    @Autowired
    private lateinit var mongoConverter: MappingMongoConverter
    @Autowired
    private lateinit var mongoDatabaseFactory: MongoDatabaseFactory

    @Bean
    fun gridsFsTemplate(): GridFsTemplate {
        return GridFsTemplate(mongoDatabaseFactory, mongoConverter);
    }
}