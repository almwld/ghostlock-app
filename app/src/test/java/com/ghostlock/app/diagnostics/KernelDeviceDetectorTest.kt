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
    fun parsesSupportedKernelFamilies() {
        assertEquals(5, KernelDeviceDetector.parseKernelMajor("5.15.119-android13-8"))
        assertEquals(6, KernelDeviceDetector.parseKernelMajor("6.1.138-android14-11"))
    }

    @Test
    fun rejectsMalformedReleaseStrings() {
        assertNull(KernelDeviceDetector.parseKernelMajor(""))
        assertNull(KernelDeviceDetector.parseKernelMajor("unknown"))
        assertNull(KernelDeviceDetector.parseKernelMajor("4"))
        assertNull(KernelDeviceDetector.parseKernelMajor("v4.19.157"))
    }
}
