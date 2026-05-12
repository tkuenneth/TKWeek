# Rule to prevent the main startup crash with Time4J's resource loader.
# This class is loaded via reflection and must be kept explicitly.
-keep class net.time4j.android.spi.AndroidResourceLoader { <init>(); }

# General rule for Time4J's Android-specific functionality.
# This keeps the main entry point and its inner classes (like the TimezoneChangedReceiver).
-keep class net.time4j.android.ApplicationStarter { *; }
