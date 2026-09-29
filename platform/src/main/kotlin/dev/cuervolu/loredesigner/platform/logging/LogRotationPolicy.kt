package dev.cuervolu.loredesigner.platform.logging

/**
 * Bounds for [RotatingFileLogWriter]. Rotation is size-based; retention enforces both a maximum
 * archive count and a maximum total size across the active file and its archives.
 */
data class LogRotationPolicy(
    val maxFileSizeBytes: Long,
    val maxArchivedFiles: Int,
    val maxTotalSizeBytes: Long,
) {
    companion object {
        val Default = LogRotationPolicy(
            maxFileSizeBytes = 2L * 1024 * 1024,
            maxArchivedFiles = 5,
            maxTotalSizeBytes = 10L * 1024 * 1024,
        )
    }
}
