package analyzer

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import org.springframework.stereotype.Service
import runner.Runner
import java.io.StringReader
import com.google.gson.JsonObject as GsonJsonObject

@Service
class AnalyzerService {
    private val objectMapper = ObjectMapper()
    private val gson = Gson()

    fun analyze(req: AnalyzeRequest): List<DiagnosticDTO> {
        val version = normalizeVersion(req.version)
        val reader = StringReader(req.source)
        val runner = Runner(version, reader)

        // Convertir el config a com.google.gson.JsonObject (que es lo que espera el Runner)
        val rulesJson: GsonJsonObject =
            if (req.config.isNullOrEmpty()) {
                GsonJsonObject()
            } else {
                convertMapToGsonJsonObject(req.config)
            }

        val result = runner.analyze(rulesJson)

        // Convertir los warnings/errors a DiagnosticDTO
        val diagnostics = mutableListOf<DiagnosticDTO>()

        result.warnings.forEach { warning ->
            diagnostics.add(
                DiagnosticDTO(
                    code = "WARNING",
                    message = warning,
                    severity = "WARNING",
                    line = 0,
                    column = 0,
                    suggestions = emptyList(),
                ),
            )
        }

        result.errors.forEach { error ->
            diagnostics.add(
                DiagnosticDTO(
                    code = "ERROR",
                    message = error,
                    severity = "ERROR",
                    line = 0,
                    column = 0,
                    suggestions = emptyList(),
                ),
            )
        }

        return diagnostics
    }

    private fun convertMapToGsonJsonObject(map: Map<String, Any?>): GsonJsonObject {
        // Convertir el Map a JSON string y luego a Gson JsonObject
        val jsonString = gson.toJson(map)
        return gson.fromJson(jsonString, GsonJsonObject::class.java)
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
