package linter

import com.google.gson.Gson
import org.springframework.stereotype.Service
import runner.Runner
import java.io.StringReader
import com.google.gson.JsonObject as GsonJsonObject

@Service
class LinterService {
    private val gson = Gson()

    fun analyze(
        version: String,
        code: String,
        rules: Map<String, Any?>?,
    ): List<String> {
        val reader = StringReader(code)
        val runner = Runner(version, reader)

        // Convertir el config a com.google.gson.JsonObject (que es lo que espera el Runner)
        val rulesJson: GsonJsonObject =
            if (rules.isNullOrEmpty()) {
                GsonJsonObject()
            } else {
                convertMapToGsonJsonObject(rules)
            }

        val result = runner.analyze(rulesJson)
        return result.warnings
    }

    private fun convertMapToGsonJsonObject(map: Map<String, Any?>): GsonJsonObject {
        // Convertir el Map a JSON string y luego a Gson JsonObject
        val jsonString = gson.toJson(map)
        return gson.fromJson(jsonString, GsonJsonObject::class.java)
    }
}
