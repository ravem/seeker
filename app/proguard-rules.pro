# Seeker ProGuard Rules

# Keep OUI database class
-keep class com.seeker.app.data.oui.** { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.seeker.app.**$$serializer { *; }
-keepclassmembers class com.seeker.app.** { *** Companion; }
-keepclasseswithmembers class com.seeker.app.** { kotlinx.serialization.KSerializer serializer(...); }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep EncryptedSharedPreferences / Tink
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
