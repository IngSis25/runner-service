package app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["linter"])
class RunnerServiceApplication

fun main(args: Array<String>) {
    runApplication<RunnerServiceApplication>(*args)
}
