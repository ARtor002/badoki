# قوانین ProGuard — فعلاً پروژه بدون ابهام‌سازی ساخته می‌شود
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.drsalam.app.api.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
