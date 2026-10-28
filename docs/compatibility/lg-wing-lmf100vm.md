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

These values and the build fingerprint recorded near the end of this report were supplied earlier by the user. They have not been independently collected from the handset in this session. The Android security patch level and exact installed-kernel-to-source mapping remain unverified.

## Repository checks

- The profile schema documents `kernel_major` as limited to 5 or 6; the Kotlin profile validator likewise marks any value other than 5 or 6 invalid. A 4.19 kernel therefore cannot be represented as a valid executable profile through the current schema without changing compatibility assumptions.
- The app requires an exact `uname -r` match for supported built-in profiles. A release string alone is not proof that a kernel image matches a profile or that a vulnerability remains present.
- The current repository implementation of `isKernelSupported()` returns true only when the full runtime `os.version` string exactly matches a built-in profile name or an imported-profile release. This is a release-name gate, not a vulnerability check; the import validator and profile schema remain separate required checks.
- No LG Wing / `LM-F100VM` / `4.19.157-perf+` executable profile was found in the branch search performed for this assessment.
- The LG open-source kernel repository is available at [LG-OSS-Kernels/android_kernel_lge_lmf100vm](https://github.com/LG-OSS-Kernels/android_kernel_lge_lmf100vm). Its branch/source must be compared against the exact installed build before drawing conclusions.
- The inspected LG source branch's top-level `Makefile` declares `VERSION = 4`, `PATCHLEVEL = 19`, and `SUBLEVEL = 157`, matching the reported `4.19.157` base release. This strengthens the source comparison but still does not prove the installed vendor build came from this exact branch revision.

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

## Follow-up source inspection (2026-10-23)

A read-only inspection was performed against the published LG kernel repository branch
`lmf100vm_android_t_vendor`, file `kernel/locking/rtmutex.c`. In that branch's
`remove_waiter()`, the implementation still takes `current->pi_lock`, removes
the waiter from the lock, clears `current->pi_blocked_on`, and passes `current`
to `rt_mutex_adjust_prio_chain()`. Those are the operations the upstream
CVE-2026-43499 fix changes to operate on `waiter->task`.

This is evidence that **the inspected public source branch appears to contain the
pre-fix implementation**. It is not proof that the kernel installed on this phone
was built from that exact branch revision: the device's full build fingerprint and
kernel build-to-source mapping still need confirmation, and vendors can backport
fixes into other revisions. Therefore the installed device remains
`DETECTED_UNVALIDATED`, not confirmed vulnerable or execution-eligible.

Source inspected:
- [LG kernel source: kernel/locking/rtmutex.c at lmf100vm_android_t_vendor](https://github.com/LG-OSS-Kernels/android_kernel_lge_lmf100vm/blob/lmf100vm_android_t_vendor/kernel/locking/rtmutex.c)
- [Upstream CVE record](https://www.cve.org/CVERecord?id=CVE-2026-43499)
- [Upstream Linux CVE announcement](https://kernel.googlesource.com/pub/scm/linux/security/vulns/+/104f5df494e2b3a98cefcda25ff01cdead552511/cve/published/2026/CVE-2026-43499.mbox)

This follow-up is a source-level compatibility finding only. No exploit offsets,
runtime memory targets, or instructions for privilege escalation are included.

## Previously supplied build identifiers

The user had previously supplied the following diagnostic values in the project history. They are recorded here to avoid asking for the same information again; this connector session did not collect them directly from the handset:

- Build fingerprint: `lge/winglm/winglm:11/RKQ1.210420.001/2215913507061:user/release-keys`
- Build ID: `RKQ1.210420.001`
- Reported kernel build date: `2022-06-08`
- Android security patch level: not present in the retrievable project notes.

These identifiers narrow the search but do not, by themselves, prove which source commit produced the installed kernel. The source/build mapping remains the key unresolved item.

## Firmware-catalogue cross-check (2026-10-23)

A public third-party firmware catalogue lists a Verizon Android 11 package named
`F100VM20f_01_VZW_US_OP_0608.kdz` for the LG Wing LM-F100VM family, with a reported
release date of 2022-06-30 and MD5 `ad6c4e8d201d33cd2809121ca5bca873`. Another public
firmware index lists the same package/hash under the `LMF100VMY` / `LMF100VMYN`
variants and gives a push date of 2022-06-28. These dates are consistent with the
reported V20f / June 2022 generation, but the catalogue is not an LG source-to-build
manifest.

The handset's previously supplied factory string is
`LMF100AT-00-V20f-LAO-COM-JUN-08-2022-ARB00+1`, while the catalogue filename identifies
a Verizon (`VZW_US`) package. The shared `V20f` label and nearby dates are insufficient
to equate these builds; the region/carrier identifiers differ, and the exact KDZ or
kernel image hash for the handset has not been established. Therefore this is a
search lead, not confirmation of the installed image's source revision or patch state.

References:
- [Public firmware record for F100VM20f_01_VZW_US_OP_0608](https://www.lg-firmwares.com/downloads-file/26955/F100VM20f_01_VZW_US_OP_0608)
- [Public LG Wing firmware index](https://info.gresslg.tk/?dir=LG+WING)

The profile-schema rule remains unchanged: `kernel_major` is restricted to 5 or 6,
and a 4.19.157 report cannot be turned into an executable profile by copying a template
or relaxing validation. Keep this record diagnostic-only unless a supported, defensive
validation path is independently established.


## KernelSU-Next build audit (2026-10-28)

A separate bilingual evidence record now tracks the local boot-image and CI artifact checks:

- [KernelSU-Next build and boot-image audit](lg-wing-kernelsu-next-build-audit.md)
- [中文审计记录](lg-wing-kernelsu-next-build-audit_ZH.md)

The recorded kernel, ramdisk, and DTB hashes match the components compared during the local audit. This does **not** validate the full boot image, the recovery DTBO, device-tree provenance, GhostLock execution eligibility, or successful boot. Run #13's artifact omitted standalone DTB/DTBO files and its log reported a missing `arch/arm64/boot/dts/` directory. Run #16 was still in progress when the audit was recorded. Keep the device status `DETECTED_UNVALIDATED` until independent source/build mapping and device validation are complete.
