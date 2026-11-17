# 4. الشاشة الجديدة والثيم

## المدينة
- خذها من المنطقة الزمنية بجهازه (ويندوز `tzutil /g`، ماك `readlink /etc/localtime`) وأكّدها معاه بسطر. الافتراضي مدينة الكويت (29.3759، 47.9774). لو منطقة ثانية بالكويت أو مدينة ثانية: الإحداثيات من معرفتك أو من Aladhan.

## اللانشر
- `python launcher-patch/apply.py ~/cleanyourtv <lat> <lon>`: يضيف سطر نور (المناسبة فوق الساعة، وتحتها الهجري والطقس والأذان الجاي). السطر يقرا كل شي من تطبيق الكهف، فركّب الكروت بالمرحلة 5 بعد اللانشر.
- بيئة أدوات CleanYourTV: `python -m venv ~/cleanyourtv/tools/venv` وبعدها pip من `tools/venv/Scripts/pip` (ويندوز) أو `tools/venv/bin/pip` (ماك) `install -r ~/cleanyourtv/tools/requirements.txt`.
- التركيب: بالماك `cd ~/cleanyourtv && TV_HOST=<ip> bash launcher/build-and-install.sh`. بالويندوز استخدم نسخة PowerShell من `~/cleanyourtv/tools/` إذا موجودة، أو `launcher/gradlew.bat assembleRelease` و`adb install -r` وبعدها `adb shell cmd package set-home-activity com.gathin.tvhome/.MainActivity`.
- لو طلع «بيئة الإعداد ماسكة الشاشة الرئيسية» (setupwraith بقوقل تي في): ارجع لـ`debloat.md`، قسم اللانشر الأصلي.
- تأكد إنه صار الرئيسي: اضغط Home وبعدها `adb shell dumpsys activity activities | grep mFocusedApp` لازم يطلع `com.gathin.tvhome`. وجرّب Home من داخل تطبيق.
- الحين تقدر تعطّل اللانشر الأصلي: `python scripts/disable.py <ip> <مجلد> --home-checked <pkg>`.

## الثيم
- وريه `docs/themes.jpg` وخله يختار:
  - `zakhrafa`: أخضر وزخرفة ذهبية، أزرار أيقونات دايرية، 4 تطبيقات بالصف.
  - `masjid-layl`: سما كحلية وهلال ومسجد، 5 تطبيقات بلا شريط.
  - `awqat`: مسجد بالصحرا، والخلفية تتغيّر مع الصبح والعصر والليل.
  - `plain`: بلا خلفية. ولو عنده صورة، حطها `wallpaper.jpg` (1920×1080) بمجلد الثيم.
- `python scripts/make_theme.py <الثيم> ~/noor-theme [~/cleanyourtv/examples/<موديله>/config.json]`. الشريط فيه كروت نور السبعة ونتفلكس ويوتيوب، و`shamsi_date: true` هو السطر اللي يكتب فيه نور.
- ارفعه: `cd ~/cleanyourtv && TV_HOST=<ip> tools/theme.sh ~/noor-theme` (أو `tools/theme.ps1` بالويندوز).
- ⚠️ إعادة تركيب اللانشر تمسح الثيم، فارفعه من جديد بعد كل تركيب.
- لو يبي تطبيق ثاني بالشريط: ضيفه لـ`dock_apps` بـ`~/noor-theme/config.json` وارفع الثيم.
