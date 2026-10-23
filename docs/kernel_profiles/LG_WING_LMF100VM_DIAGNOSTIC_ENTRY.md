# LG Wing diagnostic catalogue entry

The LG Wing `LM-F100VM` record for the reported Linux kernel release `4.19.157-perf+` is stored at:

- `app/src/main/assets/kernel_diagnostics/index.json`

It is intentionally **not** registered in `app/src/main/assets/kernel_profiles/index.conf`. `BuiltinProfileCatalog` reads only the latter, which is the executable-profile catalogue. Putting an unvalidated 4.19 record in that index would conflate diagnostic identification with executable profile support.

`DiagnosticKernelCatalog` loads the separate diagnostic index and matches the exact release plus the device codename or model. It hard-codes `executionEligible = false` for every entry, regardless of asset contents. The entry contains no offsets, route parameters, credential geometry, or exploit values.

Recorded facts:

- Manufacturer: `LGE`
- Model: `LM-F100VM`
- Device/product: `winglm`
- Android: 11 / SDK 30
- ABI: `arm64-v8a`
- Kernel release: `4.19.157-perf+`
- Status: `DETECTED_UNVALIDATED`

This entry makes the release available to diagnostic identification code; it does not claim that the installed firmware matches a particular source revision or that GhostLock supports executing against this kernel. Executable-profile validation remains unchanged.
