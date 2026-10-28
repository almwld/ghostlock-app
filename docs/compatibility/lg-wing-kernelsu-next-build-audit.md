# LG Wing KernelSU-Next build and boot-image audit

**Status: DIAGNOSTIC ONLY — not verified for flashing or GhostLock execution.**  
Audit date: 2026-10-28

This record consolidates the LG Wing LM-F100 / SM7250 kernel-build and boot-image checks performed in Termux and GitHub Actions. It records evidence and unresolved items; it does not register an executable GhostLock profile or establish that the handset is vulnerable.

## Source build and artifact provenance

- Kernel source repository: [almwld/android_kernel_lge_sm7250](https://github.com/almwld/android_kernel_lge_sm7250)
- Build branch: `ksunext-winglm-ci`
- Artifact-producing run: [Run #13](https://github.com/almwld/android_kernel_lge_sm7250/actions/runs/40608527655)
- Run #13 source commit: `01a96ec6e19ac2e74b96e6317cf9e563ad8460fa`
- Artifact name: `winglm-kernelsu-next-13`
- Artifact ID: `12783115056`
- Artifact SHA-256: `fff1b40bc5f75d462512ccd79d555b7f7f14fdd95df482aef289357a1ef2cab6`
- The ZIP in `~/downloads/winglm-kernelsu-next.zip` and the copy in `~/LG_Wing_Root_Project_Archive/winglm-kernelsu-next.zip` were independently hashed and matched this SHA-256.
- Artifact expiration shown by GitHub at audit time: 2026-11-11. Prefer preserving the ZIP and its checksum in the local project archive.

Run #13 produced a kernel image and diagnostics. Its ZIP listing showed `Image`, `Image.gz`, `kernel.config`, `build.log`, and `SHA256SUMS`; it did **not** include a standalone DTB or DTBO file. The build log contained `find: ‘arch/arm64/boot/dts/’: No such file or directory` while still proceeding to create `Image` and `Image.gz`. Therefore the successful kernel-image step must not be interpreted as proof that device-tree outputs were built and packaged.

## Local boot-image evidence

Local file: `~/LG_Wing_Root_Project_Archive/boot_new_base0.img`

The image was successfully parsed with `unpack_bootimg`. Reported header fields:

- Magic: `ANDROID!`
- Header version: 2
- Page size: 4096
- Kernel size: 46,503,952 bytes
- Ramdisk size: 27,225,758 bytes
- Recovery DTBO size: 3,056,833 bytes
- DTB size: 1,212,782 bytes
- Kernel load address: `0x8000`
- Ramdisk load address: `0x01000000`
- DTB address: `0x01f00000`
- Reported OS patch level: `2127-12` — anomalous/future-looking and not independently validated; do not reuse it as a trusted build value without checking the original image metadata.

Extracted-component SHA-256 values:

| Component | SHA-256 | Comparison result |
|---|---|---|
| Extracted kernel | `8c6ced13dc396002c76addeb6b6f9d2bf016cf48aa977a7ed769bc8ed38e8ff5` | Exactly matches the uncompressed `Image` extracted from the Run #13 ZIP |
| Extracted ramdisk | `b63aeb0373b90a6f04534f74721bdd9d6db25ec257bdc87109c0c87d55351199` | Exactly matches local `ramdisk_magisk` |
| Extracted DTB component | `322de263c6463877c82b037c634b743c4ccb104aa439f9e5eabfad36e1032910` | Exactly matches local `dtb_magisk` |
| Extracted `recovery_dtbo` | `80256ffbe91bb1b4099966c238f4befd5a65e90001fe7d533036da5245f2d727` | No independent trusted reference comparison yet |
| Original reference kernel | `9d0cf9a74991a44ca0e7c912a5ef5b93b20980c3115f4e42084e1a0f1c292f62` | Different from the newly built kernel, as expected for a modified build |

The whole local `boot_new_base0.img` SHA-256 recorded in the project archive is `72b29f53d71eeeb3a5e216d49311731363825df9a749e79b6241037da6643940`. The same hash was previously confirmed for the local and external archive copies.

The DTB inspection utility identified an FDT structure size of 605,249 bytes within the extracted 1,212,782-byte DTB component. This observation alone does not prove corruption; the component may contain additional data or multiple structures. Do not replace the component based on this discrepancy alone.

## KernelSU configuration evidence

The packaged `kernel.config` included `CONFIG_KSU=y`, `CONFIG_KPROBES=y`, and `CONFIG_MODULES=y`, with `CONFIG_KSU_SYSCALL_TABLE_HOOK=y`. Strings such as `kernelsu.allow_shell`, `kernelsu.norc`, and `Tksu_avc` were found in the kernel image. These are useful supporting indicators, but config values and strings alone do not prove runtime activation, correct manager integration, or successful root behavior.

## Newer CI runs and validation changes

- [Run #16](https://github.com/almwld/android_kernel_lge_sm7250/actions/runs/40617303191), commit `8ef0c4fffb3dc65a47f4cf9ea582b02aa6933027`, remained `in_progress` at the latest check; the kernel/device-tree build step was still running and verification/artifact upload had not started.
- [Run #17](https://github.com/almwld/android_kernel_lge_sm7250/actions/runs/40621283683), commit `55e037666dea3d96af3360fd0c8ec1e8153459d6`, ended with conclusion `failure` immediately and the GitHub jobs endpoint returned no jobs. The available metadata does not establish the cause; do not label it a compile failure without runner logs.
- The workflow was updated in commit `55e037666dea3d96af3360fd0c8ec1e8153459d6` to fail verification when zero DTB/DTBO files are collected and to hash every collected artifact, not just `Image.gz`. This stricter check is intended to prevent a kernel-only output from being reported as a complete device build.
- No artifact was listed for Run #16 at the latest check, and Run #17 exposed no jobs/artifacts. Neither run currently provides a verified, complete device build.

## Decision and required follow-up

**Do not flash `boot_new_base0.img` based on this evidence alone.** The kernel, ramdisk, and DTB comparisons prove component identity, not full-device boot compatibility. The recovery DTBO has not been compared to a trusted source, and Run #13 did not package device-tree outputs.

Before considering any boot-image test:

1. Confirm a final successful run and inspect its complete artifact listing, including whether expected device-tree outputs exist and are non-empty.
2. Compare candidate DTB/DTBO files with a trusted image for the exact device variant and firmware build; do not substitute `recovery_dtbo` merely because a newly built file exists.
3. Establish the source commit corresponding to the handset's actual vendor kernel and confirm the boot-header values from the original image.
4. Preserve the original boot image and a verified recovery path. The handset was reported as bootloader-locked, so a flash attempt must not be assumed possible or safe.
5. Keep GhostLock device status as `DETECTED_UNVALIDATED`. Do not create or enable an executable profile based on the kernel version or this build audit. Kernel-source observations do not prove that the installed vendor kernel has or lacks a backported fix.

This report is an evidence ledger only. It contains no exploit offsets, runtime memory targets, or executable GhostLock profile.
