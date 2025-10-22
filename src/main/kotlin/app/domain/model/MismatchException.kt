package app.domain.model

class VersionMismatchException(
    val requested: String,
    val actual: String
) : RuntimeException("Requested version=$requested but snippet has version=$actual")

