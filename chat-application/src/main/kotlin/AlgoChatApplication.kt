import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication(
    scanBasePackages = [
        "com.chat.application",
        "com.chat.domain"
    ]
)
@EnableJpaAuditing
@EntityScan(basePackages = ["com.chat.domain.domain.entity"])
class AlgoChatApplication

fun main(args: Array<String>) {
    runApplication<AlgoChatApplication>(*args)
}