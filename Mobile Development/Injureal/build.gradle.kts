plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.android.library") version "8.7.3" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.16" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
