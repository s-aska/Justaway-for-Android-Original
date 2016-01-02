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

# for Gson
-keepattributes Signature
-keepattributes *Annotation*

-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }

# http://qiita.com/petitviolet/items/1b709f3f0db2659a271a
-keepnames class info.justaway.model.** { *; }

# for Twitter4j
-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

# *** Debug ***
# -renamesourcefileattribute SourceFile
# -keepattributes SourceFile,LineNumberTable
# *** Debug ***
