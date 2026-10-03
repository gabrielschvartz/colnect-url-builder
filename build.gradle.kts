// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}

// Auto-restore debug.keystore from debug.keystore.base64 if missing on local developer machines
val debugKeystore = file("${rootDir}/debug.keystore")
val debugKeystoreBase64 = file("${rootDir}/debug.keystore.base64")
if (!debugKeystore.exists() && debugKeystoreBase64.exists()) {
  try {
    val cleanBase64 = debugKeystoreBase64.readText().replace("\r", "").replace("\n", "").trim()
    val decoded = java.util.Base64.getDecoder().decode(cleanBase64)
    debugKeystore.writeBytes(decoded)
  } catch (e: Exception) {
    project.logger.warn("Could not auto-restore debug.keystore: ${e.message}")
  }
}
