# NexsusAI ProGuard Rules

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }

# Room
-keep class com.nexusai.data.local.** { *; }

# Ktor - keep classes but dontwarn for Android-incompatible references
-keep class io.ktor.** { *; }
-dontwarn java.lang.management.**
-dontwarn java.lang.reflect.**
-dontwarn org.slf4j.**

# Domain models
-keep class com.nexusai.domain.model.** { *; }
-keep class com.nexusai.domain.ai.** { *; }

# AI Provider classes
-keep class com.nexusai.data.ai.** { *; }
