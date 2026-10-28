package com.ghostlock.app.diagnostics

import android.os.Build
import android.system.Os

/**
 * Read-only device/kernel identification for diagnostics.
 *
 * This class deliberately does not select, load, or authorize a kernel profile.
 * Device identification is not evidence that a profile is safe or compatible.
 */
object KernelDeviceDetector {
    enum class Compatibility {
        PROFILE_MAY_BE_MATCHED,
        RELEASE_DETECTED_UNVALIDATED,
        UNKNOWN_KERNEL_RELEASE
    }

    data class Report(
        val manufacturer: String,
        val model: String,
        val device: String,
        val product: String,
        val fingerprint: String,
        val androidRelease: String,
        val sdkInt: Int,
        val supportedAbis: List<String>,
        val kernelRelease: String,
        val kernelMajor: Int?,
        /** True when a non-empty release was read; this grants no execution permission. */
        val canReadOrEditDiagnosticMetadata: Boolean,
        val compatibility: Compatibility,
        val explanation: String
    )

    /** Collects runtime facts without launching a shell or modifying device state. */
    fun detect(): Report {
        val release = runCatching { normalizeKernelRelease(Os.uname().release.orEmpty()) }
            .getOrElse { normalizeKernelRelease(System.getProperty("os.version").orEmpty()) }
        val major = parseKernelMajor(release)
        val compatibility = classifyKernelRelease(release)
        val explanation = when (compatibility) {
            Compatibility.UNKNOWN_KERNEL_RELEASE ->
                "Kernel release could not be reliably read; profile matching must remain disabled."
            Compatibility.RELEASE_DETECTED_UNVALIDATED ->
                "Kernel release $release was read and preserved, but profile compatibility has not been validated. Diagnostic metadata may be inspected or edited independently."
            Compatibility.PROFILE_MAY_BE_MATCHED ->
                "Kernel family detected. Exact release/profile validation is still required; this is not a support guarantee."
        }

        return Report(
            manufacturer = Build.MANUFACTURER.orEmpty(),
            model = Build.MODEL.orEmpty(),
            device = Build.DEVICE.orEmpty(),
            product = Build.PRODUCT.orEmpty(),
            fingerprint = Build.FINGERPRINT.orEmpty(),
            androidRelease = Build.VERSION.RELEASE.orEmpty(),
            sdkInt = Build.VERSION.SDK_INT,
            supportedAbis = Build.SUPPORTED_ABIS?.toList().orEmpty(),
            kernelRelease = release,
            kernelMajor = major,
            canReadOrEditDiagnosticMetadata = release.isNotBlank(),
            compatibility = compatibility,
            explanation = explanation
        )
    }

    /**
     * Detection and metadata editing are independent from execution support.
     * A valid release outside documented profile families is retained as
     * detected-but-unvalidated, never rejected for read/edit work.
     */
    internal fun classifyKernelRelease(release: String): Compatibility {
        val major = parseKernelMajor(release)
        return when {
            release.isBlank() || major == null -> Compatibility.UNKNOWN_KERNEL_RELEASE
            major == 5 || major == 6 -> Compatibility.PROFILE_MAY_BE_MATCHED
            else -> Compatibility.RELEASE_DETECTED_UNVALIDATED
        }
    }

    /** Trims surrounding whitespace but preserves the full vendor release suffix. */
    internal fun normalizeKernelRelease(release: String): String = release.trim()

    /** Parses only the leading numeric major in a Linux release string. */
    internal fun parseKernelMajor(release: String): Int? {
        val match = Regex("""^\s*(\d+)\.\d+(?:\.|[-+])""").find(release)
            ?: return null
        return match.groupValues.getOrNull(1)?.toIntOrNull()
    }
}
