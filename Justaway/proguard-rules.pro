-dontwarn sun.misc.Unsafe

# for Butter Knife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# for EventBus
-keepclassmembers class ** {
    public void onEvent*(**);
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*

-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }

# Twitter4j
-dontwarn twitter4j.**
-keep class twitter4j.** { *; }
