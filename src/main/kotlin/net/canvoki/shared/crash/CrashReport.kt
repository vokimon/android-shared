package net.canvoki.shared.crash

import java.io.Serializable

data class CrashReport(
    val fullText: String,
    val summary: String,
) : Serializable
