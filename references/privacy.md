# 3. الخصوصية

ما تنتجاوز. التلفزيونات الذكية فيها خدمة تتعرّف على اللي يطلع بالشاشة (ACR) وترسله للشركة للإعلانات.

## من القائمة (هو يسويها بالريموت، وأنت تأكد بصورة شاشة)
- الإعدادات ← الخصوصية أو «حول»: اطفِ «معلومات المشاهدة» أو «Viewing information» أو «Smart TV experience» أو «Live Plus» (حسب الماركة).
- الإعدادات ← الخصوصية ← الإعلانات: «حذف معرّف الإعلانات» أو «إلغاء تخصيص الإعلانات».
- قوقل تي في: الإعدادات ← الخصوصية ← «الاستخدام والتشخيص» ← اطفه.

## من adb (بإذنه)
- دوّر على حزم التتبّع: `adb shell pm list packages | grep -i -E "acr|alphonso|samba|inscape|tvinteractive|viewing|adservice|analytics|tracking"`.
- اللي تأكدت إنه تتبّع (من `dumpsys package` أو من قائمة CleanYourTV لموديله): عطّله بـ`disable.py` مثل أي تطبيق.
- التطبيقات اللي ماسكة المايك أو الكاميرا وهي مو المساعد الصوتي:
  `adb shell appops query-op RECORD_AUDIO allow` و`adb shell appops query-op CAMERA allow`.
  اسحبها بإذنه: `adb shell appops set <pkg> RECORD_AUDIO ignore`، واكتبها بملف `appops-by-us.txt` بمجلد النسخة عشان ترجعها بـ`allow`.
- لا تلمس المساعد الصوتي إذا قال إنهم يستخدمونه.
