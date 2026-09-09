# NEXIQ Production R8 / ProGuard Configuration

# Preserve annotations and keep-annotated classes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Domain & Data Models (Serialization and Reflection safety)
-keep class com.screentranslator.domain.model.** { *; }

# Google ML Kit (Text Recognition & Translation)
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# OkHttp & HTTP Networking
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }
