package com.ghostlock.app.diagnostics

import android.util.AtomicFile
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

/**
 * Stores editable diagnostic text separately from executable/imported profiles.
 *
 * Documents use a .txt suffix and live only in the diagnostics directory. This
 * store never parses HOCON, registers a profile, or supplies data to native code.
 */
class DiagnosticProfileTextStore(filesDir: File) {
    private val directory = File(filesDir, "kernel_diagnostics")

    init {
        require(directory.isDirectory || directory.mkdirs()) {
            "Unable to create diagnostic document directory"
        }
    }

    /** Lists diagnostic text documents without loading or resolving them. */
    @Synchronized
    fun listDocuments(): List<String> =
        directory.listFiles()
            .orEmpty()
            .asSequence()
            .filter { it.isFile && it.name.endsWith(SUFFIX) }
            .map { it.name.removeSuffix(SUFFIX) }
            .sorted()
            .toList()

    /** Returns the exact UTF-8 text saved under [name], or null if absent. */
    @Synchronized
    fun readDocument(name: String): String? {
        val target = target(name)
        if (!target.isFile) return null
        require(target.length() <= MAX_DOCUMENT_BYTES) { "Diagnostic document is too large" }
        return AtomicFile(target).openRead().use { input ->
            input.readBytes().toString(StandardCharsets.UTF_8)
        }
    }

    /**
     * Atomically saves exact UTF-8 diagnostic text. It is intentionally not an
     * executable profile import and is never passed to the native process.
     */
    @Synchronized
    fun writeDocument(name: String, content: String) {
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        require(bytes.size <= MAX_DOCUMENT_BYTES) { "Diagnostic document is too large" }
        val atomicFile = AtomicFile(target(name))
        var output: FileOutputStream? = null
        try {
            output = atomicFile.startWrite()
            output.write(bytes)
            atomicFile.finishWrite(output)
        } catch (failure: Throwable) {
            if (output != null) atomicFile.failWrite(output)
            throw failure
        }
    }

    @Synchronized
    fun deleteDocument(name: String): Boolean {
        val target = target(name)
        val atomicFile = AtomicFile(target)
        if (!target.exists() && !File(target.path + ".bak").exists()) return false
        atomicFile.delete()
        return true
    }

    private fun target(name: String): File {
        require(NAME_PATTERN.matches(name) && name != "." && name != "..") {
            "Invalid diagnostic document name"
        }
        val target = File(directory, name + SUFFIX)
        require(target.parentFile?.canonicalFile == directory.canonicalFile) {
            "Diagnostic document must stay inside its directory"
        }
        return target
    }

    private companion object {
        const val SUFFIX = ".txt"
        const val MAX_DOCUMENT_BYTES = 256 * 1024
        val NAME_PATTERN = Regex("[A-Za-z0-9._-]{1,80}")
    }
}
