# LG Wing LM-F100VM compatibility assessment

**Status: DETECTED_UNVALIDATED — not execution-eligible**

This is a read-only compatibility record, not an executable kernel profile.

## Device data supplied for this assessment

- Model: `LM-F100VM`
- Device codename: `winglm`
- Reported kernel release: `4.19.157-perf+`
- Android: 11
- Bootloader: locked
- Verified Boot: green
- SELinux: enforcing

These values are user-reported and have not been independently collected from a device in this session. The complete build fingerprint, security patch level, and exact installed kernel build remain unverified.

## Repository checks

- The profile schema documents `kernel_major` as limited to 5 or 6; the Kotlin profile validator likewise marks any value other than 5 or 6 invalid. A 4.19 kernel therefore cannot be represented as a valid executable profile through the current schema without changing compatibility assumptions.
- The app requires an exact `uname -r` match for supported built-in profiles. A release string alone is not proof that a kernel image matches a profile or that a vulnerability remains present.
- No LG Wing / `LM-F100VM` / `4.19.157-perf+` executable profile was found in the branch search performed for this assessment.
- The LG open-source kernel repository is available at [LG-OSS-Kernels/android_kernel_lge_lmf100vm](https://github.com/LG-OSS-Kernels/android_kernel_lge_lmf100vm). Its branch/source must be compared against the exact installed build before drawing conclusions.

## CVE assessment

CVE-2026-43499 concerns incorrect task handling in the Linux `rtmutex` waiter-removal path. Upstream advisory material identifies the affected logic in `kernel/locking/rtmutex.c` and documents fixes in newer stable branches. The broad upstream version history does **not** prove that a vendor kernel is vulnerable: Android vendors can backport fixes without changing the reported base version.

Authoritative references:
- [Linux kernel CVE announcement](https://kernel.googlesource.com/pub/scm/linux/security/vulns/+/104f5df494e2b3a98cefcda25ff01cdead552511/cve/published/2026/CVE-2026-43499.mbox)
- [Ubuntu CVE-2026-43499 status](https://ubuntu.com/security/CVE-2026-43499)
- [LG LM-F100VM kernel source](https://github.com/LG-OSS-Kernels/android_kernel_lge_lmf100vm)

## Current conclusion

**Compatibility is unconfirmed.** The reported 4.19 kernel major version is outside GhostLock's documented profile schema, and no validated profile for this exact device/build was found. Do not infer that the device is exploitable from the version string or create a runnable profile from guessed values.

## Safe next validation steps

1. Collect the exact device build fingerprint, kernel release, Android security patch level, and build number from read-only device settings/diagnostics.
2. Identify the precise LG source tag/commit corresponding to that build, if published.
3. Have the vendor source reviewed for whether the upstream fix was backported; if source/build correspondence cannot be established, retain the status as unvalidated.
4. Keep the device outside execution eligibility until the evidence is independently verified. This report does not include exploit offsets, memory targets, or instructions to obtain root.
