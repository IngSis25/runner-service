package runner.validate

import org.springframework.stereotype.Service
import runner.Runner
import java.io.StringReader

// valida el código y devuelve una lista de errores encontrados
@Service
class ValidateService {
    fun validate(
        version: String,
        code: String,
    ): List<String> {
        val reader = StringReader(code)
        val runner = Runner(version, reader)
        val result = runner.validate()
        return result.errors
    }
}
