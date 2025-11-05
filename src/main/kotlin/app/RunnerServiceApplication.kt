package app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["linter", "app", "interpreter"])
class RunnerServiceApplication

fun main(args: Array<String>) {
    runApplication<RunnerServiceApplication>(*args)
}
