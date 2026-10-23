# LG Wing 5G (LM-F100VM) compatibility status

**Status: unsupported / not validated. Do not run an imported or hand-edited profile on this device.**

## Device facts recorded for triage

- Model: LG Wing 5G `LM-F100VM`
- Device codename: `winglm`
- Product variant: `winglm.NA3_US` / `NA3_US`
- Android: 11 (SDK 30), ARM64
- Reported kernel release: `4.19.157-perf+`
- Reported fingerprint: `lge/winglm/winglm:11/RKQ1.210420.001/2215913507061:user/release-keys`
- Boot state reported: locked; verified boot green; SELinux enforcing.

## Why this is not a profile-only change

The current profile schema documents `kernel_major` as 5 or 6. Kernel 4.19 is therefore outside the supported schema and execution assumptions. Changing the schema enum or copying a profile from another 4.19 device would not establish compatibility: vendor kernels can backport individual changes and have device-specific structure layouts, configuration, address mapping, and allocator behavior.

The LG OSS source tree at [android_kernel_lge_lmf100vm](https://github.com/LG-OSS-Kernels/android_kernel_lge_lmf100vm) reports base version 4.19.157 on branch `lmf100vm_android_t_vendor`. This is useful evidence, but does not prove that its exact source revision and configuration match the installed firmware build.

The upstream issue [#324](https://github.com/YuKongA/ghostlock-app/issues/324) documents failures in a different vendor 4.19 kernel when the app followed the 6.x path, including a hard reset during testing. This is not an LG-specific result, but it demonstrates why a version-number workaround is unsafe.

## Required validation before support can be claimed

1. Match the installed firmware fingerprint to an authoritative firmware package and its exact kernel source/configuration.
2. Have maintainers define a separate, reviewed 4.x compatibility design rather than routing 4.x through 5.x or 6.x assumptions.
3. Add version-specific validation and regression tests that fail closed when kernel family or required metadata is unsupported.
4. Validate the complete compatibility path on a recoverable test device before adding any entry to the supported-device list.

## Current behavior and recommendation

No LG Wing profile is listed as supported, and no validated profile is provided by this document. Profile generation or successful import must not be interpreted as support. Until the checks above are complete, keep the device classified as unsupported and do not attempt execution with guessed offsets or a profile copied from another model.
