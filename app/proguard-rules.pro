# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, Annotation, EnclosingMethod, SourceFile
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# And if you use AsyncExecutor:
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-keep class goodtime.training.wod.timer.ui.stats.EditWeeklyGoalDialog { *; }

-keep class goodtime.training.wod.timer.data.model.WeeklyGoal { *; }
-keep class goodtime.training.wod.timer.data.model.Session { *; }
-keep class goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton { *; }
-keep class goodtime.training.wod.timer.data.model.SessionSkeleton { *; }
-keep class goodtime.training.wod.timer.data.model.SessionType { *; }
-keep class goodtime.training.wod.timer.data.db.CustomWorkoutSkeletonDao { *; }
-keep class goodtime.training.wod.timer.data.db.SessionDao { *; }
-keep class goodtime.training.wod.timer.data.db.WeeklyGoalDao { *; }
-keep class goodtime.training.wod.timer.data.db.SessionSkeletonDao { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder

-keep, allowobfuscation, allowoptimization class org.kodein.di.TypeReference
-keep, allowobfuscation, allowoptimization class * extends org.kodein.di.TypeReference

-keep class androidx.databinding.** { *; }
-keepclassmembers class * {
    @androidx.databinding.BindingAdapter *;
}
-keepclassmembers interface * {
    @androidx.databinding.BindingAdapter *;
}

# Gson uses generic type information stored in a class file when working with
# fields. Proguard removes such information by default, keep it.
-keepattributes Signature

# This is also needed for R8 in compat mode since multiple
# optimizations will remove the generic signature such as class
# merging and argument removal. See:
# https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#troubleshooting-gson-gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepattributes AnnotationDefault,RuntimeVisibleAnnotations