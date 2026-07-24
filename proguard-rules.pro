# Xposed module - keep entry points
-keep class com.devicefaker.HookInit { *; }
-keep class de.robv.android.xposed.** { *; }
-keepattributes *Annotation*