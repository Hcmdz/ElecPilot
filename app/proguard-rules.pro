# ── App ──────────────────────────────────────────────────
-keep class com.HcmDz.ElecPilot.MainActivity { *; }
-keep class com.HcmDz.ElecPilot.ui.viewmodel.** { *; }

# ── Log stripping (release) ──────────────────────────────
-assumenosideeffects class android.util.Log {
    public static int d(java.lang.String, java.lang.String);
    public static int v(java.lang.String, java.lang.String);
    public static int i(java.lang.String, java.lang.String);
    public static int e(java.lang.String, java.lang.String);
    public static int e(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int w(java.lang.String, java.lang.String);
    public static int w(java.lang.String, java.lang.String, java.lang.Throwable);
}

# ── Room ─────────────────────────────────────────────────
-keep class com.HcmDz.ElecPilot.data.db.** { *; }
-dontwarn androidx.room.paging.**

# ── Optimize (from centic9/poi-on-android) ──────────────
-optimizations !field/*,!class/merging/*,*
-mergeinterfacesaggressively

# ── Apache POI (shadow jar from centic9/poi-on-android) ──
-dontwarn org.apache.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn org.etsi.**
-dontwarn org.w3.**
-dontwarn com.microsoft.schemas.**
-dontwarn javax.naming.**
-dontwarn java.lang.management.**
-dontwarn org.slf4j.impl.**
-dontwarn java.awt.**
-dontwarn org.apache.logging.log4j.**

-dontnote org.apache.**
-dontnote org.openxmlformats.schemas.**
-dontnote org.etsi.**
-dontnote org.w3.**
-dontnote com.microsoft.schemas.**
-dontnote javax.naming.**
-dontnote java.lang.management.**
-dontnote org.slf4j.impl.**

-keeppackagenames org.apache.poi.ss.formula.function

-keep,allowoptimization,allowobfuscation class org.apache.logging.log4j.** { *; }
-keep,allowoptimization class org.apache.commons.compress.archivers.zip.** { *; }
-keep,allowoptimization class org.apache.poi.schemas.** { *; }
-keep,allowoptimization class org.apache.xmlbeans.** { *; }
-keep,allowoptimization class org.openxmlformats.schemas.** { *; }
-keep,allowoptimization class com.microsoft.schemas.** { *; }

# ── Woodstox / StAX (missing from Android) ──────────────
-keep class com.fasterxml.woodstox.** { *; }
-keep class org.codehaus.woodstox.** { *; }
-dontwarn com.fasterxml.woodstox.**
-dontwarn org.codehaus.woodstox.**
-dontwarn javax.xml.stream.**

# ── Kotlin Coroutines ────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── Optional deps ───────────────────────────────────────
-dontwarn aQute.bnd.annotation.baseline.BaselineIgnore
-dontwarn aQute.bnd.annotation.spi.ServiceConsumer
-dontwarn aQute.bnd.annotation.spi.ServiceProvider
-dontwarn com.github.luben.zstd.ZstdInputStream
-dontwarn edu.umd.cs.findbugs.annotations.Nullable
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
-dontwarn java.awt.Shape
-dontwarn org.osgi.framework.Bundle
-dontwarn org.osgi.framework.BundleContext
-dontwarn org.osgi.framework.FrameworkUtil
-dontwarn org.osgi.framework.ServiceReference
-dontwarn org.osgi.framework.wiring.BundleRevision
-dontwarn org.tukaani.xz.MemoryLimitException
-dontwarn org.tukaani.xz.SingleXZInputStream
-dontwarn org.tukaani.xz.XZInputStream

# ── rclone (native binary) ──────────────────────────────
-keep class com.HcmDz.ElecPilot.util.RcloneDriveService { *; }

# ── javax / Apache HTTP (missing classes) ────────────────
-dontwarn org.ietf.jgss.**
-dontwarn org.apache.http.**
-keep class org.apache.http.** { *; }
