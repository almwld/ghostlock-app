package com.ghostlock.app.diagnostics

import android.content.Context
import org.json.JSONObject

/**
 * Loads diagnostic device records from a dedicated asset catalogue.
 *
 * This catalogue is intentionally separate from kernel_profiles/index.conf.
 * Entries here can identify a release for display/triage only; they cannot
 * become executable profiles or supply values to the native process.
 */
object DiagnosticKernelCatalog {
    private const val ASSET_PATH = "kernel_diagnostics/index.json"
    private const val EXPECTED_PURPOSE = "diagnostic-identification-only"

    data class Entry(
        val id: String,
        val manufacturer: String,
        val model: String,
        val device: String,
        val product: String,
        val androidRelease: String,
        val sdkInt: Int,
        val abi: String,
        val kernelRelease: String,
        val status: String,
        val notes: String,
        /** Diagnostic entries are never eligible for execution. */
        val executionEligible: Boolean = false,
    )

    /** Returns an empty list if the optional diagnostic catalogue is absent or invalid. */
    fun load(context: Context): List<Entry> = runCatching {
        val text = context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        parse(text)
    }.getOrDefault(emptyList())

    /**
     * Matches the full kernel release and at least one supplied device identity.
     * This is display/diagnostic matching only and grants no profile or execution permission.
     */
    fun findMatch(
        context: Context,
        kernelRelease: String,
        device: String,
        model: String,
    ): Entry? {
        if (kernelRelease.isBlank()) return null
        return load(context).firstOrNull { entry ->
            entry.kernelRelease == kernelRelease.trim() &&
                ((device.isNotBlank() && entry.device.equals(device.trim(), ignoreCase = true)) ||
                    (model.isNotBlank() && entry.model.equals(model.trim(), ignoreCase = true)))
        }
    }

    internal fun parse(text: String): List<Entry> {
        val root = JSONObject(text)
        require(root.optInt("schema_version", -1) == 1) { "Unsupported diagnostic catalogue schema" }
        require(root.optString("purpose") == EXPECTED_PURPOSE) {
            "Unexpected catalogue purpose"
        }
        val profiles = root.optJSONArray("profiles") ?: return emptyList()
        return buildList {
            for (index in 0 until profiles.length()) {
                val item = profiles.optJSONObject(index) ?: continue
                val id = item.optString("id").trim()
                val release = item.optString("kernel_release").trim()
                val device = item.optString("device").trim()
                val model = item.optString("model").trim()
                if (id.isEmpty() || release.isEmpty() || (device.isEmpty() && model.isEmpty())) {
                    continue
                }
                add(
                    Entry(
                        id = id,
                        manufacturer = item.optString("manufacturer"),
                        model = model,
                        device = device,
                        product = item.optString("product"),
                        androidRelease = item.optString("android_release"),
                        sdkInt = item.optInt("sdk_int", 0),
                        abi = item.optString("abi"),
                        kernelRelease = release,
                        status = item.optString("status", "DETECTED_UNVALIDATED"),
                        notes = item.optString("notes"),
                        // Never trust an asset field to grant execution eligibility.
                        executionEligible = false,
                    ),
                )
            }
        }
    }
}
