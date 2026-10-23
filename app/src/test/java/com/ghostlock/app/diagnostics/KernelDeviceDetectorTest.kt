package com.ghostlock.app.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KernelDeviceDetectorTest {
    @Test
    fun parsesLgWingKernelMajor() {
        assertEquals(4, KernelDeviceDetector.parseKernelMajor("4.19.157-perf+"))
    }

    @Test
    fun detectsLgWingKernelWithoutTreatingDetectionAsExecutionSupport() {
        assertEquals(
            KernelDeviceDetector.Compatibility.RELEASE_DETECTED_UNVALIDATED,
            KernelDeviceDetector.classifyKernelRelease("4.19.157-perf+")
        )
    }

    @Test
    fun preservesExactReleaseSuffixForDiagnosticAndEditingUse() {
        val release = "4.19.157-perf+"
        assertEquals(release, release.trim())
        assertEquals(
            KernelDeviceDetector.Compatibility.RELEASE_DETECTED_UNVALIDATED,
            KernelDeviceDetector.classifyKernelRelease(release)
        )
    }

    @Test
    fun knownProfileFamiliesStillRequireExactProfileMatching() {
        assertEquals(
            KernelDeviceDetector.Compatibility.PROFILE_MAY_BE_MATCHED,
            KernelDeviceDetector.classifyKernelRelease("5.15.119-android13-8")
        )
        assertEquals(
            KernelDeviceDetector.Compatibility.PROFILE_MAY_BE_MATCHED,
            KernelDeviceDetector.classifyKernelRelease("6.1.138-android14-11")
        )
    }

    @Test
    fun unknownAndMalformedReleasesStayFailClosed() {
        assertEquals(
            KernelDeviceDetector.Compatibility.UNKNOWN_KERNEL_RELEASE,
            KernelDeviceDetector.classifyKernelRelease("")
        )
        assertEquals(
            KernelDeviceDetector.Compatibility.UNKNOWN_KERNEL_RELEASE,
            KernelDeviceDetector.classifyKernelRelease("unknown")
        )
        assertNull(KernelDeviceDetector.parseKernelMajor("4"))
        assertNull(KernelDeviceDetector.parseKernelMajor("v4.19.157"))
    }
}
