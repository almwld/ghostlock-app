# LG Wing 5G (LM-F100VM) compatibility status

**Status: kernel identification is implemented; profile/execution support for Linux 4.19 remains unsupported and unvalidated.**

## Device facts recorded for triage

- Model: LG Wing 5G `LM-F100VM`
- Device codename: `winglm`
- Product variant: `winglm.NA3_US` / `NA3_US`
- Android: 11 (SDK 30), ARM64
- Reported kernel release: `4.19.157-perf+`
- Reported fingerprint: `lge/winglm/winglm:11/RKQ1.210420.001/2215913507061:user/release-keys`
- Boot state reported: locked; verified boot green; SELinux enforcing.

## Implemented: read-only runtime identification

`KernelDeviceDetector.kt` reads Android build identity and ABI data, then obtains the kernel release through `android.system.Os.uname()` (with a JVM OS-version fallback). It parses the major version and classifies the release for diagnostics.

The detector intentionally separates **detected** from **supported**:
- `4.19.157-perf+` is recognized as a valid Linux kernel release with major version 4.
- Major version 4 remains `UNSUPPORTED_KERNEL_FAMILY` in the current profile path.
- Major 5 or 6 only means a profile may be matched; exact-release and field validation remain required.
- An unreadable or malformed release remains `UNKNOWN_KERNEL_RELEASE`.

Unit tests cover these cases. The detector does not select a profile, change device state, or authorize execution, and is not yet wired into a user-facing screen.

## Why 4.19 support is not a profile-only change

The current profile schema requires `kernel_major` 5 or 6. The configuration controls kernel symbol/address resolution, structure geometry, route selection, credential layout, and other native execution assumptions. Setting `kernel_major = 4`, adding the reported release to the index, or copying a profile from another 4.19 device would not validate those assumptions.

The LG OSS source tree at [android_kernel_lge_lmf100vm](https://github.com/LG-OSS-Kernels/android_kernel_lge_lmf100vm) reports base version 4.19.157 on branch `lmf100vm_android_t_vendor`. This does not prove its exact source revision/configuration matches the installed firmware.

The upstream issue [#324](https://github.com/YuKongA/ghostlock-app/issues/324) documents failures on a different vendor 4.19 kernel when routed through a 6.x path, including a hard reset. This is not an LG-specific result, but illustrates why forcing an unsupported kernel through a different family is unsafe.

## Remaining work before support can be claimed

1. Match the installed firmware to an authoritative package and exact kernel source/configuration.
2. Review the current Kotlin/native code paths and determine whether a separate 4.x implementation is feasible.
3. Define and test fail-closed validation for any future supported 4.x profile.
4. Validate any future compatibility implementation on a recoverable test device before marking the model supported.

## Current behavior

The detector can identify the reported kernel family, but **GhostLock does not have validated execution support for the LG Wing kernel**. No runnable profile is provided by this document. Keep the device unsupported until the implementation and validation work is complete; do not use guessed offsets or a profile copied from another model.
