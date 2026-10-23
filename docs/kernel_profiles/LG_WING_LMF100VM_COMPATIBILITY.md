# LG Wing 5G (LM-F100VM) compatibility status

**Status: device identification added; kernel profile remains unsupported / not validated. Do not run an imported or hand-edited profile on this device.**

## Device facts recorded for triage

- Model: LG Wing 5G `LM-F100VM`
- Device codename: `winglm`
- Product variant: `winglm.NA3_US` / `NA3_US`
- Android: 11 (SDK 30), ARM64
- Reported kernel release: `4.19.157-perf+`
- Reported fingerprint: `lge/winglm/winglm:11/RKQ1.210420.001/2215913507061:user/release-keys`
- Boot state reported: locked; verified boot green; SELinux enforcing.

## Implemented: read-only runtime identification

Added `app/src/main/java/com/ghostlock/app/diagnostics/KernelDeviceDetector.kt`.

The detector reads Android's runtime build identity and ABI list, then obtains the kernel release through `android.system.Os.uname()` (falling back to the JVM OS version string if unavailable). It parses the leading kernel major and returns a diagnostic status. It does not launch a shell, modify the device, select a profile, or authorize execution.

A unit test covers the reported `4.19.157-perf+` format, representative 5.x/6.x releases, and malformed strings:
`app/src/test/java/com/ghostlock/app/diagnostics/KernelDeviceDetectorTest.kt`.

**Integration note:** this is a diagnostic component; it has not yet been wired into a user-facing screen or the profile-loading/execution flow. Its result is not proof of profile compatibility.

## Why this is not a profile-only change

The current profile set and execution assumptions are centered on 5.x and 6.x. Kernel 4.19 is therefore not validated for the current execution path. Changing a schema enum or copying a profile from another 4.19 device would not establish compatibility: vendor kernels can backport individual changes and have device-specific structure layouts, configuration, address mapping, and allocator behavior.

The LG OSS source tree at [android_kernel_lge_lmf100vm](https://github.com/LG-OSS-Kernels/android_kernel_lge_lmf100vm) reports base version 4.19.157 on branch `lmf100vm_android_t_vendor`. This is useful evidence, but does not prove that its exact source revision and configuration match the installed firmware build.

The upstream issue [#324](https://github.com/YuKongA/ghostlock-app/issues/324) documents failures in a different vendor 4.19 kernel when the app followed the 6.x path, including a hard reset during testing. This is not an LG-specific result, but it demonstrates why a version-number workaround is unsafe.

## Required validation before support can be claimed

1. Match the installed firmware fingerprint to an authoritative firmware package and its exact kernel source/configuration.
2. Have maintainers define and review whether a separate 4.x compatibility design is appropriate.
3. Add version-specific validation and regression tests that fail closed when kernel family or required metadata is unsupported.
4. Validate any future compatibility work on a recoverable test device before adding an entry to the supported-device list.

## Current behavior and recommendation

No validated LG Wing profile is provided here. Profile generation or successful import must not be interpreted as support. Until the checks above are complete, keep the device classified as unsupported and do not attempt execution with guessed offsets or a profile copied from another model.
