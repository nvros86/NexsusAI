# Add project specific ProGuard rules here.
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep class com.nexusai.data.local.** { *; }
-keep class io.ktor.** { *; }
-keep class com.nexusai.domain.model.** { *; }
-keep class com.nexusai.data.ai.** { *; }
