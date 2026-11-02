# kotlinx.serialization: keep generated serializers
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.muneeb.quran16line.** {
    *** Companion;
}
-keepclasseswithmembers class com.muneeb.quran16line.** {
    kotlinx.serialization.KSerializer serializer(...);
}
