package printscript.runner_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RunnerServiceApplication

fun main(args: Array<String>) {
    runApplication<RunnerServiceApplication>(*args)
}
