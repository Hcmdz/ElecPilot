<!-- Sync with EN rev 792beb6 (2026-10-12) -->
# ElecPilot
<a id="readme-top"></a>

[![ElecPilot](../../docs/assets/feature-graphic.png)](https://github.com/Hcmdz/ElecPilot/releases/latest)

**اقرأ هذا بلغات أخرى**

<div dir="ltr">

🇺🇸 [English](../../README.md) | 🇫🇷 [Français](README.fr-FR.md) | 🇸🇦 [العربية](README.ar-SA.md)

</div>
<!-- Localized app: values-fr/values-ar (+ldrtl) mirror README langs; supportsRtl verified. -->

<div align="center">
   <a href="https://github.com/Hcmdz/ElecPilot/releases">
      <img src="../../get-it-on-github-v2.png" width="168" height="50" alt="Get it on GitHub">
   </a>
   <a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/Hcmdz/ElecPilot/">
      <img src="../../get-it-on-obtainium-v2.png" width="168" height="50" alt="Get it on Obtainium">
   </a>
</div>

<br />

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![MinSDK](https://img.shields.io/badge/MinSDK-29-orange.svg)](#)
[![TargetSDK](https://img.shields.io/badge/TargetSDK-36-blue.svg)](#)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Release](https://img.shields.io/github/v/release/Hcmdz/ElecPilot)](https://github.com/Hcmdz/ElecPilot/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Hcmdz/ElecPilot/total)](https://github.com/Hcmdz/ElecPilot/releases)
[![Stars](https://img.shields.io/github/stars/Hcmdz/ElecPilot)](https://github.com/Hcmdz/ElecPilot/stargazers)
[![Forks](https://img.shields.io/github/forks/Hcmdz/ElecPilot)](https://github.com/Hcmdz/ElecPilot/network/members)
[![Compose M3](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

تطبيق أندرويد لإدارة مقلعات المحركات الكهربائية ووحدات الإدخال/الإخراج PLC في البيئات الصناعية.

في الميدان، بيانات المحركات تعيش على الورق أو في جداول مبعثرة — ضائعة، قديمة، غير قابلة للبحث. ElecPilot هو دفتر الميدان الذي يعمل دون اتصال: كل مقلع وكل وحدة PLC مسجلة مرة واحدة، تُوجد خلال ثوانٍ، وتُحفظ نسختها مشفرة.

- **Package**: `com.HcmDz.ElecPilot`
- **Version**: 6.5.4 (versionCode 34)
- **Author**: HcmDZ — [@Hcmdz](https://github.com/Hcmdz)

---

<div dir="ltr">

<p align="center">
  <a href="#لقطات-الشاشة">عرض الديمو</a>
  ·
  <a href="https://github.com/Hcmdz/ElecPilot/issues/new?labels=bug">الإبلاغ عن خطأ</a>
  ·
  <a href="https://github.com/Hcmdz/ElecPilot/issues/new?labels=enhancement">اقتراح ميزة</a>
</p>

</div>

<details>
<summary>جدول المحتويات</summary>

- [📦 التنزيلات](#-التنزيلات)
- [لقطات الشاشة](#لقطات-الشاشة)
- [المزايا الرئيسية](#المزايا-الرئيسية)
- [التقنيات والبنية](#️-التقنيات-والبنية)
- [البدء](#-البدء)
- [الاختبارات](#-الاختبارات)
- [بنية المشروع](#-بنية-المشروع)
- [Apache POI Shadow Jar](#apache-poi-shadow-jar)
- [التوقيع](#التوقيع)
- [حجم APK](#حجم-apk)
- [سجل التغييرات](#سجل-التغييرات-v57--v654)
- [قانوني](#قانوني)
- [الرخصة](#الرخصة)
- [خارطة الطريق](#️-خارطة-الطريق)
- [مستندات ذات صلة](#مستندات-ذات-صلة)
- [تواصل](#-تواصل)
</details>

---

## 📦 التنزيلات

احصل على ElecPilot من GitHub: **[أحدث إصدار](https://github.com/Hcmdz/ElecPilot/releases/latest)** (`ElecPilot-release.apk`، ~13.3 MB، `arm64-v8a`).

[![Download](https://img.shields.io/badge/Download-Latest_Release-green.svg?logo=android)](https://github.com/Hcmdz/ElecPilot/releases/latest)

- يتطلب Android 9+ (API 29) مع **arm64-v8a**؛ اسمح بـ *تثبيت التطبيقات غير المعروفة* للمتصفح عند الطلب.
- تحقق من السلامة: `sha256sum -c ElecPilot-release.apk.sha256` (ملف sidecar بجانب APK).
- ⚠️ إذا كنت قادمًا من نسخة ≤ 6.5: يلزم إعادة تثبيت يدوية (مفتاح توقيع جديد منذ 6.5.1 — انسخ احتياطيًا، ألغِ التثبيت، ثبّت، استعد).

<p align="right">(<a href="#readme-top">العودة للأعلى</a>)</p>

---

## لقطات الشاشة

جد أي مقلع خلال ثوانٍ، بالوضع الفاتح أو الداكن — القوائم والتفاصيل والإعدادات بنظرة واحدة.

### مقلعات المحركات

<div align="center">

| قائمة المقلعات — عرض الجدول (الوضع الفاتح) | قائمة المقلعات — عرض الجدول (الوضع الداكن) |
|---|---|
| <a href="../../screenshots/motor-list-table-view-light.png"><img src="../../screenshots/motor-list-table-view-light.png" width="270" alt="قائمة المقلعات — عرض الجدول, الوضع الفاتح"></a> | <a href="../../screenshots/motor-list-table-view-dark.png"><img src="../../screenshots/motor-list-table-view-dark.png" width="270" alt="قائمة المقلعات — عرض الجدول, الوضع الداكن"></a> |
| قائمة المقلعات — عرض البطاقات (الوضع الفاتح) | تفاصيل المقلع (الوضع الداكن) |
| <a href="../../screenshots/motor-list-card-view-light.png"><img src="../../screenshots/motor-list-card-view-light.png" width="270" alt="قائمة المقلعات — عرض البطاقات, الوضع الفاتح"></a> | <a href="../../screenshots/motor-detail-dark.png"><img src="../../screenshots/motor-detail-dark.png" width="270" alt="تفاصيل المقلع, الوضع الداكن"></a> |

</div>

### وحدات PLC للإدخال/الإخراج

كل وحدة مسجلة — عرض جدول أو بطاقات، مع التفاصيل الكاملة لكل وحدة.

<div align="center">

| قائمة وحدات PLC — عرض البطاقات (الوضع الفاتح) | قائمة وحدات PLC — عرض البطاقات (الوضع الداكن) |
|---|---|
| <a href="../../screenshots/plc-list-card-view-light.png"><img src="../../screenshots/plc-list-card-view-light.png" width="270" alt="قائمة وحدات PLC — عرض البطاقات, الوضع الفاتح"></a> | <a href="../../screenshots/plc-list-card-view-dark.png"><img src="../../screenshots/plc-list-card-view-dark.png" width="270" alt="قائمة وحدات PLC — عرض البطاقات, الوضع الداكن"></a> |
| قائمة وحدات PLC — عرض الجدول (الوضع الفاتح) | تفاصيل وحدة PLC (الوضع الداكن) |
| <a href="../../screenshots/plc-list-table-view-light.png"><img src="../../screenshots/plc-list-table-view-light.png" width="270" alt="قائمة وحدات PLC — عرض الجدول, الوضع الفاتح"></a> | <a href="../../screenshots/plc-detail-dark.png"><img src="../../screenshots/plc-detail-dark.png" width="270" alt="تفاصيل وحدة PLC, الوضع الداكن"></a> |

</div>

### الإعدادات

النسخ الاحتياطي واللغة والتحديثات — كل شيء قابل للضبط في مكان واحد.

<div align="center">

| الإعدادات (الوضع الفاتح) | الإعدادات (الوضع الداكن) |
|---|---|
| <a href="../../screenshots/settings-light.png"><img src="../../screenshots/settings-light.png" width="270" alt="الإعدادات, الوضع الفاتح"></a> | <a href="../../screenshots/settings-dark.png"><img src="../../screenshots/settings-dark.png" width="270" alt="الإعدادات, الوضع الداكن"></a> |

</div>

<p align="right">(<a href="#readme-top">العودة للأعلى</a>)</p>

---

## المزايا الرئيسية

### مقلعات المحركات
- **كل مقلع مسجل.** CRUD كامل، بحث، تحرير جماعي، إحصائيات.
- **بحث بدون استخدام اليدين.** تحدث لتصفية المقلعات بالبحث الصوتي.
- **ابدأ من ورقة نظيفة.** قوالب Excel جاهزة للتعبئة للمقلعات ووحدات PLC.

### وحدات PLC للإدخال/الإخراج
- **كل وحدة مسجلة.** قاعدة بيانات وعروض مخصصة لوحدات PLC.

### النسخ الاحتياطي والمزامنة
- **بياناتك تنجو من الهاتف.** نسخ احتياطي محلي مجدول إلى مجلد تختاره، إضافة إلى نسخ احتياطي يدوي في أي وقت.
- **أثر ورقي عند الطلب.** نسخ محلي مع تصدير/استيراد Excel وCSV ونسخ مجدولة.

### التطبيق
- **محدث دائمًا.** تحديث داخل التطبيق مع فحص تلقائي وتنزيل من GitHub Releases.
- **يبدو في بيته.** سمة Material You بلون ديناميكي، edge-to-edge.
- **يتحدث لغتك.** توطين النظام/EN/FR/AR مع اكتشاف تلقائي للغة الجهاز.
- **محصّن افتراضيًا.** FLAG_SECURE، حظر cleartext، تعطيل نسخ Android الاحتياطية، تجريد سجلات ProGuard.

<p align="right">(<a href="#readme-top">العودة للأعلى</a>)</p>

---

## 🛠️ التقنيات والبنية

### البنية

- تطبيق **أحادي الوحدة** مع `MainActivity` + تنقل Jetpack Compose
- طبقة البيانات: قواعد Room + نمط Repository
- ViewModels مع StateFlow
- WorkManager للنسخ المجدولة (محلية + سحابية)

### المكتبات والأدوات الأساسية

| الفئة | المكتبة | الإصدار |
|---|---|---|
| **UI** | Jetpack Compose + Material 3 | BOM 2026.09.00 |
| **Activity** | Activity Compose | 1.13.0 |
| **Lifecycle** | Lifecycle Runtime Compose | 2.11.0 |
| **Async** | Kotlin Coroutines & Flow | 1.11.0 |
| **Database** | Room | 2.8.5 |
| **Networking** | OkHttp | 5.5.0 |
| **Excel** | Apache POI (shadow jar من centic9/poi-on-android) | 5.2.5 |
| **Scheduling** | WorkManager | 2.12.0 |
| **File Access** | DocumentFile (SAF) | 1.1.0 |
| **Security** | ProGuard, إعداد أمان الشبكة | — |
| **Build** | AGP 9.4.1, Kotlin 2.4.21, KSP 2.3.12 | — |
| **Lint** | Android Security Lint | 1.0.4 |
| **Quality** | Detekt CLI | 1.23.8 |
| **Testing** | JUnit4, Room Testing, Coroutines Test | 4.13.2 / 2.8.5 / 1.11.0 |

### مزايا الأمان (v6.0)

| الضبط | التنفيذ |
|---|---|
| **حماية لقطات الشاشة** | `FLAG_SECURE` على MainActivity |
| **تجريد السجلات** | ProGuard يزيل `Log.d/v/i/e/w` (بما فيها stack traces) في نسخ release |
| **أمان الشبكة** | Cleartext محظور (`network_security_config.xml`)، شهادات النظام فقط |
| **النسخ الاحتياطي معطل** | `allowBackup="false"` |
| **MTE** | `memtagMode="sync"` مفعّل في manifest |

### الأذونات

- `INTERNET`، `ACCESS_NETWORK_STATE` — التحقق من التحديثات وتنزيلها
- `POST_NOTIFICATIONS` — تقدم النسخ وحالة التحديثات
- `REQUEST_INSTALL_PACKAGES` — تثبيت التحديثات داخل التطبيق

نقاط الدخول: `MainActivity` (launcher)، `FileProvider` (`${applicationId}.fileprovider`).

### CI والجودة

- GitHub Actions: `.github/workflows/ci.yml` (`testDebugUnitTest` + `lintDebug`)، `codeql.yml`، Dependabot
- Detekt: `./gradlew detekt` (الإعداد `config/detekt/detekt.yml`)
- أسماء متغيرات CI المطلوبة (القيم تبقى في أسرار المستودع/البيئة ولا تُحفظ في الـ commits أبدًا): بيانات اعتماد keystore الـ release (`RELEASE_STORE_FILE`، `RELEASE_STORE_PASSWORD`، `RELEASE_KEY_ALIAS`، `RELEASE_KEY_PASSWORD`)

<p align="right">(<a href="#readme-top">العودة للأعلى</a>)</p>

---

## 🚀 البدء

### المتطلبات

- **Android Studio**: الأحدث المستقر (Meerkat أو أحدث) — IDE ([doc](https://developer.android.com/studio))
- **JDK**: 17 — Gradle toolchain ([doc](https://docs.gradle.org/current/userguide/build_java_projects.html))
- **Android SDK**: compileSdk 37 ([التثبيت](https://developer.android.com/studio#downloads))

### بناء APK

```bash
cd "ElecPilot"
./gradlew assembleRelease
```

الناتج: `app/build/outputs/apk/release/ElecPilot-release.apk`

### الاستخدام اليومي

```bash
./gradlew installDebug   # run on device
./gradlew test           # unit tests
./gradlew lintDebug      # static analysis
```

### التشغيل على جهاز

1. افتح المشروع في **Android Studio**.
2. انتظر اكتمال مزامنة Gradle.
3. اختر جهازًا أو محاكيًا يعمل بـ **API 29+**.
4. اضغط **Run** (Shift + F10) أو:
   ```bash
   ./gradlew installDebug
   ```

<p align="right">(<a href="#readme-top">العودة للأعلى</a>)</p>

---

## 🧪 الاختبارات

```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## 📁 بنية المشروع

```
app/src/main/java/com/HcmDz/ElecPilot/
├── MainActivity.kt              # Single activity, edge-to-edge, FLAG_SECURE
├── data/
│   ├── db/                      # Room databases (Motor + PLC)
│   ├── repository/              # Data repositories
│   ├── BackupPreferences.kt     # Local backup settings models
│   ├── CloudBackupPreferences.kt # Cloud backup settings models
│   └── CloudBackupFileInfo.kt   # Cloud file metadata
├── ui/
│   ├── screens/                 # Compose screens (Main, MotorDetail, dialogs)
│   ├── viewmodel/               # ViewModels
│   ├── components/              # Reusable UI components
│   ├── theme/                   # Material 3 theming
│   └── views/plc/               # PLC-specific views
├── util/
│   ├── BackupManager.kt         # Local backup (Excel/CSV)
│   ├── ExcelUtil.kt             # Apache POI export/import + templates
│   ├── UpdateManager.kt         # In-app update (GitHub Releases)
│   ├── ContextUtils.kt          # Locale-aware context helpers
│   ├── NotificationHelper.kt    # Backup notifications
│   ├── BackupScheduler.kt       # WorkManager: local backup schedule
│   └── CloudBackupScheduler.kt  # WorkManager: cloud backup schedule
└── worker/
    └── BackupWorker.kt          # WorkManager: local backup
    └── CloudBackupWorker.kt     # WorkManager: cloud backup
```

---

## Apache POI Shadow Jar

يُضمَّن Apache POI كـ shadow jar مبني مسبقًا من [centic9/poi-on-android](https://github.com/centic9/poi-on-android) بدلًا من اعتمادية Maven عادية.

### لماذا shadow jar؟

POI الخام من Maven مع R8 (مصغّر أندرويد) مكسور جوهريًا. تشويش R8 يكسر تحليل أسماء الأصناف `SchemaTypeSystemImpl` في xmlbeans، وتقليص R8 يولّد أصناف bridge اصطناعية مكسورة. يحل shadow jar ذلك عبر:

- نقل `javax.xml.stream` → `org.apache.poi.javax.xml.stream` (أندرويد يفتقد API الكاملة لـ StAX)
- نقل `javax.xml.namespace` → `org.apache.poi.javax.xml.namespace`
- استبدال أصناف `java.awt.*` المفقودة بـ stubs
- تضمين تنفيذ StAX [aalto-xml](https://github.com/FasterXML/aalto-xml)
- دمج كل اعتمادات POI في jar واحد

يوجد shadow jar في `app/libs/poishadow-all.jar` (~19.5 MB خام). يزيل R8 أصناف POI غير المستخدمة في builds الـ release.

### خصائص النظام

تتطلب محللات StAX المنقولة خصائص نظام تُضبط قبل أي كود POI. يُدار ذلك في `ExcelUtil.kt`:

```kotlin
System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl")
System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl")
System.setProperty("org.apache.poi.ss.ignoreMissingFontSystem", "true")
```

### تحديث shadow jar

لبناء نسخة أحدث:

```bash
git clone https://github.com/centic9/poi-on-android.git
cd poi-on-android
./gradlew :poishadow:shadowJar
cp poishadow/build/libs/poishadow-all.jar <path-to-ElecPilot>/app/libs/
```

فرع master في centic9/poi-on-android يستخدم POI 5.5.1. الـ release المبنية مسبقًا (5.2.5-4) تستخدم POI 5.2.5. كلتاهما تعملان لقراءة/كتابة xlsx الأساسية.

---

## التوقيع

APK الـ release موقّع بـ keystore. لبناء APK release:

1. أنشئ ملف `gradle.properties` في جذر المشروع (متجاهَل من git أصلًا)
2. أضف خاصيتي المسار أدناه، إضافةً إلى خاصيتي كلمة المرور بالاسم نفسه:

```properties
RELEASE_STORE_FILE=/path/to/your/release.keystore
RELEASE_KEY_ALIAS
```

خاصيتا كلمة المرور هما `RELEASE_STORE_PASSWORD` و`RELEASE_KEY_PASSWORD`.
امنحهما قيمتهما محليًا، أو وفّرها من بيئة العمل. لا تُودع أي قيمة هنا.

3. شغّل `./gradlew assembleRelease`

---

## حجم APK

حجم APK الـ release الحالي: **13.3 MB** (13 948 290 بايت، `arm64-v8a` فقط، R8 مفعّل) — مقيس على ملف v6.5.3 المنشور.

| المحتوى | غير مضغوط | النسبة |
|---|---|---|
| كود التطبيق (dex، R8 + Compose + Room + OkHttp) | ~11.6 MB | 76 % |
| موارد Apache POI / xmlbeans | ~3.0 MB | 20 % |
| موارد وأصول أخرى | ~0.6 MB | 4 % |
| مكتبات أصلية | ~10 KB | <1 % |

تقنيات التقليل المطبقة:
- POI shadow jar مع R8 لإزالة أصناف Apache POI/xmlbeans غير المستخدمة
- `abiFilters` مقيد بـ `arm64-v8a` لـ builds الـ release
- `isMinifyEnabled = true` مع `isShrinkResources = true` وقواعد ProGuard من centic9/poi-on-android

كان حجم APK 20.3 MB قبل v6.5.3؛ إزالة ثنائي rclone الأصلي أزالت نحو 7 MB منه.

---

## سجل التغييرات (v5.7 → v6.5.4)

تتبع النسخ [semver](https://semver.org/)؛ السجل الكامل في [GitHub Releases](https://github.com/Hcmdz/ElecPilot/releases).

### v6.5.4

- **اكتمل حذف النسخ السحابي** — أُزيلت الملفات المتبقية ونقطة دخول OAuth واستثناء الشبكة المحلية
- **تصحيح التوثيق** — قائمة المزايا وجدول الأمان والأذونات وحجم APK تصف الآن ما يفعله التطبيق فعليًا
- موقّع بنفس شهادة v6.5.3، لذا التثبيت تحديث عادي في المكان

### v6.5.3

- **حذف النسخ السحابي** — يطلب Google الآن من كل تطبيق استخدام مشروع Cloud خاص به لـ OAuth، ولم يعد المشروع المشترك الذي اعتمد عليه rclone مقبولًا
- **حجم APK من 20.3 MB إلى 13.3 MB** — أُزيل الثنائي الأصلي لـ rclone المضمّن
- دون تغيير: النسخ المحلي المجدول، الاستيراد/التصدير CSV/Excel، البحث، المرشحات، الإحصاءات، البحث الصوتي، التحديث داخل التطبيق، EN/FR/AR

### v6.5.2

- **تطبيق لغة النظام دون إعادة تشغيل** — العودة إلى خيار *النظام* تتبع الآن لغة النظام الحالية بدلاً من اللغة المحددة سابقًا

### v6.5.1

- **rclone 1.75.1 مخفف** — backends local/Drive/OneDrive فقط
- **مفتاح توقيع جديد** — يلزم إعادة تثبيت يدوية (انسخ احتياطيًا، ألغِ التثبيت، ثبّت، استعد)

### v6.5

- **عمل خلفية قابل للإلغاء بأمان** — يُعاد رمي `CancellationException` في مسارات النسخ السحابي/المحلي والتحقق من التحديث والاستيراد (لا إشعار فشل كاذب ولا retry بعد الإلغاء)
- **كتابات snapshot على Main** — حوارات النسخ تعيد إسناد حالة UI على الخيط الرئيسي
- **أسماء ملفات نسخ ASCII** — الطوابع الزمنية للتصدير/السحابة بـ `Locale.US` (لا أرقام غير لاتينية تحت locale العربية)
- **تلميع بطاقات PLC** — حجم نجمة المفضلة موحد مع بطاقات المحركات (18.dp)
- **Build** — AGP 9.4.0، OkHttp 5.5.0، WorkManager 2.11.2

### v6.3

- **لا شاشة بيضاء عند الإقلاع البارد** — الشاشة الكاملة مشروطة بـ `hasLoadedOnce` فلا يُرسم الهيكل الفارغ (ترويسة بعداد « 0 ») قبل أول تحميل
- **آخر وحدة persistent** — التطبيق يعيد الفتح على آخر وحدة نشطة (Départs / PLC I/O) بعد إعادة التشغيل البارد

### v6.2

- **تحديث داخل التطبيق** — فحص تلقائي لـ GitHub Releases، تنزيل وتثبيت مع تقدم
- **توليد قوالب فارغة** — قوالب Excel جاهزة للتعبئة (ترويسات EN/FR)
- **خيار لغة النظام** — اكتشاف تلقائي للغة الجهاز عند أول تثبيت
- **إصلاحات** — معالجة CancellationException، تعقيم أسماء الملفات، عتبات التحقق من الترويسات، حارس طابور التصدير
- **تدقيق أمني** — تحصين مزايا التحديث والقوالب (توصية SHA-256، منع path traversal)

### v6.0 → v6.1

إصلاحات التدقيق الأمني (OWASP MASVS 2.1):

| # | الخطورة | الإصلاح |
|---|---|---|
| 1 | حرجة | ملف rclone المؤقت: أذونات قراءة للمالك فقط |
| 2 | عالية | قائمة URL مسموحة لـ WebView OAuth ضد التصيد |
| 4 | متوسطة | كاش النسخ السحابي مشفر (AES-256-GCM) |
| 5 | متوسطة | `FLAG_SECURE` على MainActivity (حماية لقطة/تسجيل) |
| 7 | منخفضة | بديل env HOME يستخدم `filesDir` بدل مسار ثابت |
| 8 | منخفضة | استخدام صريح لـ SharedPreferences `MODE_PRIVATE` |
| 9 | منخفضة | سجلات الأخطاء منقاة من مسارات الملفات وتفاصيل العمليات |
| 10 | منخفضة | ProGuard يزيل `Log.e/w` مع كائنات الاستثناء في release |

<p align="right">(<a href="#readme-top">العودة للأعلى</a>)</p>

---

## قانوني

- [شروط الاستخدام](https://hcmdz.github.io/ElecPilot/terms/)
- [سياسة الخصوصية](https://hcmdz.github.io/ElecPilot/privacy/)

---

## الرخصة

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

هذا المشروع تحت **GNU General Public License v3.0** — انظر ملف [LICENSE](../../LICENSE) للتفاصيل.

## 🗺️ خارطة الطريق

يُتتبع العمل المخطط في [التذاكر المفتوحة](https://github.com/Hcmdz/ElecPilot/issues) — اقترح المزايا هناك.

## مستندات ذات صلة

- [Contributing](../../CONTRIBUTING.md) · [مكونات الطرف الثالث](../../THIRD_PARTY.md) · [الأمان](../../SECURITY.md)
- [سياسة الخصوصية](../../docs/privacy/) · [الشروط](../../docs/terms/)

## 📬 تواصل

HcmDZ — [@Hcmdz](https://github.com/Hcmdz)

رابط المشروع: [https://github.com/Hcmdz/ElecPilot](https://github.com/Hcmdz/ElecPilot)

---

Made with ❤️ by HcmDZ
