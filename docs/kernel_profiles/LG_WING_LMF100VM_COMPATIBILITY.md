# LG Wing 5G (LM-F100VM) compatibility status

**Status: kernel identification and diagnostic metadata inspection/editing are independent of profile execution support. Linux 4.19 execution support remains unvalidated.**

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

The detector separates **release detection**, **diagnostic metadata editing**, and **execution compatibility**:
- The exact release string `4.19.157-perf+` is retained, including its vendor suffix and trailing `+`.
- A readable release outside the currently documented profile families is reported as `RELEASE_DETECTED_UNVALIDATED`; that state does not prevent diagnostic metadata from being inspected or edited.
- `PROFILE_MAY_BE_MATCHED` for major 5 or 6 is only a preliminary state. Exact-release and field validation remain required.
- An unreadable or malformed release remains `UNKNOWN_KERNEL_RELEASE`.

Unit tests cover the diagnostic classification. The detector does not select a profile, change device state, or authorize execution, and is not yet wired into a user-facing screen. This change does **not** relax the separate HOCON schema validation or authorize a 4.x profile to be executed.

## Read/edit access is not execution support

Kernel release identification is informational. A detected 4.19 release should not be hidden or blocked from diagnostic metadata review merely because the current execution schema only documents kernel majors 5 and 6. `DiagnosticProfileTextStore` provides bounded UTF-8 read/write storage in the app's separate `kernel_diagnostics/` directory (`.txt` documents), with safe names and atomic writes. It does not parse or import those documents as executable profiles and never sends their contents to native code. Any attempt to resolve or execute a profile remains subject to the existing schema, field, route, and exact-release validation. No guessed values are supplied by this change.

## Why 4.19 execution support is not a profile-only change

The current profile schema requires `kernel_major` 5 or 6. The configuration controls kernel symbol/address resolution, structure geometry, route selection, credential layout, and other native execution assumptions. Setting `kernel_major = 4`, adding the reported release to the index, or copying a profile from another 4.19 device would not validate those assumptions.

The LG OSS source tree at [android_kernel_lge_lmf100vm](https://github.com/LG-OSS-Kernels/android_kernel_lge_lmf100vm) reports base version 4.19.157 on branch `lmf100vm_android_t_vendor`. This does not prove its exact source revision/configuration matches the installed firmware.

The upstream issue [#324](https://github.com/YuKongA/ghostlock-app/issues/324) documents failures on a different vendor 4.19 kernel when routed through a 6.x path, including a hard reset. This is not an LG-specific result, but illustrates why forcing an unsupported kernel through a different family is unsafe.

## Remaining work before support can be claimed

1. Match the installed firmware to an authoritative package and exact kernel source/configuration.
2. Review the current Kotlin/native code paths and determine whether a separate 4.x implementation is feasible.
3. Define and test fail-closed validation for any future supported 4.x profile.
4. Validate any future compatibility implementation on a recoverable test device before marking the model supported.

## Current behavior

The detector can identify and retain the reported kernel release for diagnostics, and diagnostic metadata editing is not gated by the major version. **GhostLock does not have validated execution support for the LG Wing kernel.** No runnable profile is provided by this document. Keep the device unsupported until the implementation and validation work is complete; do not use guessed offsets or a profile copied from another model.
