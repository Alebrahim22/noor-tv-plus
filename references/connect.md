# 1. جهّز الجهاز ولاقِ التلفزيون

## الجهاز
- افحص بروحك: `adb`، JDK 17، Android SDK (`platforms;android-34` و`build-tools;34.0.0`)، `python`، `git`. وللتسجيلات بس: `yt-dlp` و`ffmpeg` (التسجيلات الافتراضية موجودة بالريبو، فما تحتاجها إلا لو بغى يغيّر الصوت).
- `python -c "import sys;sys.path.insert(0,'scripts');import tvlib;print(tvlib.find_adb())"` يقول لك وين adb.
- قل له بسطر شنو ناقص، واستأذن مرة وحدة وركّبه:
  - ويندوز: `winget install Google.PlatformTools EclipseAdoptium.Temurin.17.JDK Python.Python.3.12 Git.Git`، والـ SDK من Android Studio أو `sdkmanager`.
  - ماك: `brew install android-platform-tools openjdk@17 android-commandlinetools`، وبعدها `sdkmanager "platforms;android-34" "build-tools;34.0.0"`.
- خلّ `JAVA_HOME` يأشر على JDK 17 وقت البناء.
- نزّل CleanYourTV جنب الريبو: `git clone https://github.com/Gathin23/cleanyourtv ~/cleanyourtv`. منه اللانشر وقوائم التطبيقات الآمنة لكل ماركة.

## خيارات المطوّر (الخطوة الوحيدة اللي يسويها بإيده)
- الإعدادات ← النظام (أو تفضيلات الجهاز) ← حول ← اضغط على «بناء نظام التشغيل» (Build) 7 مرات لين يطلع «أصبحت مطوّراً».
- ارجع ← خيارات المطوّر ← فعّل «تصحيح أخطاء USB»، و«تصحيح الأخطاء عبر الشبكة» لو موجود.
- يتأكد إن الكمبيوتر والتلفزيون على نفس الواي فاي.

## لاقِ التلفزيون
- `python scripts/scan.py` يدوّر على الشبكة ويطلع العناوين اللي فاتحة بورت 5555.
- `adb connect <ip>:5555`. بيطلع بالتلفزيون «السماح بتصحيح الأخطاء؟». قل له يختار «السماح دائماً».
- لو ما فيه 5555 وفيه «تصحيح الأخطاء اللاسلكي» بس: `adb pair <ip>:<port>` بالرمز اللي على الشاشة، وبعدها `adb connect`.
- بشّره: «لقيت تلفزيونك 🎉».

## اعرف التلفزيون
- `adb -s <ip>:5555 shell getprop ro.product.model` و`ro.product.manufacturer` و`ro.build.version.release` و`ro.build.version.sdk` و`ro.build.characteristics`.
- النظام لازم أندرويد 8 أو أحدث (SDK 26). لو أقل: وقف.
- لو مو أندرويد تي في أو قوقل تي في: وقف وقل له بلطف.
- دوّر بـ`~/cleanyourtv/examples/` على ماركته وموديله. لو موجود، بشّره: «تلفزيونك مجرّب قبل 👌».
