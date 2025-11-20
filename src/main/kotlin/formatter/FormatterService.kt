package formatter

import com.google.gson.Gson
import org.springframework.stereotype.Service
import runner.Runner
import java.io.StringReader

@Service
class FormatterService {
    private val gson = Gson()

    fun format(req: FormatRequest): String {
        val version = normalizeVersion(req.version)
        val reader = StringReader(req.source)
        val runner = Runner(version, reader)

        // Convertir el config a JSON string (que es lo que espera el Runner)
        val rulesJson: String =
            if (req.config.isNullOrEmpty()) {
                "{}"
            } else {
                gson.toJson(req.config)
            }

        val result = runner.format(rulesJson, version)
        return result.formattedCode
    }

    private fun normalizeVersion(v: String): String {
        val x = v.trim().lowercase().removePrefix("v")
        return when (x) {
            "1", "1.0" -> "1.0"
            "1.1" -> "1.1"
            else -> x
        }
    }
}
