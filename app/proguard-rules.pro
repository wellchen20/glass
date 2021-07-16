
-optimizationpasses 5
-dontpreverify
-dontoptimize
-verbose
-keepattributes *Annotation*
-ignorewarning
-keepclasseswithmembernames class * {
     native <methods>;
}
-keepattributes *Annotation*
-keep enum com.ainemo.sdk.NemoSDKListener** {
    **[] $VALUES;
    public *;
}
-keepclassmembers enum * { *; }
-keepnames class * implements java.io.Serializable
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class com.ainemo.sdk.model.Settings{*;}
-keep class com.ainemo.sdk.NemoSDK{
  public *;
}
-keep class com.ainemo.**{*;}
-keep class com.llvision.**{*;}
-keep class android.utils.**{*;}
-keep class android.util.**{*;}
-keep class android.log.**{*;}
-keep class android.http.**{*;}
-keep class vulture.module.**{*;}
-keep class vulture.home.call.media.omap.**{*;}

-keep class com.google.gson.stream.** {*;}
-keep class com.google.gson.** {*;}
-keep class com.google.gson.Gson {*;}
-keep class com.google.gson.examples.android.model.** {*;}
-keep class rx.internal.util.**{*;}

# add for uvc proguard
-keepclassmembers public class com.serenegiant.usb.UVCCamera {*;}
-keep class com.serenegiant.usb.IStatusCallback{*;}
-keep class com.serenegiant.usb.IButtonCallback{*;}
-keep class com.serenegiant.usb.IFrameCallback{*;}
# end