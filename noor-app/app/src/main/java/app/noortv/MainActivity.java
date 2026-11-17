package app.noortv;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * One shortcut on the home screen. A dua card plays its recording full screen (from the Kahf app, which carries
 * the audio); the other cards open their video in the YouTube TV app. Opening the Kahf card also (re)starts the
 * adhan, the duas, the reminders and the phone page.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (BuildConfig.REMINDERS) Reminder.startAll(this);
        Duas.Dua dua = Duas.byKey(BuildConfig.DUA);
        if (dua != null) {
            DuaActivity.start(this, dua);
            finish();
            return;
        }
        Uri video = Uri.parse("https://www.youtube.com/watch?v=" + BuildConfig.VIDEO);
        Intent yt = new Intent(Intent.ACTION_VIEW, video).setPackage("com.google.android.youtube.tv");
        try {
            startActivity(yt);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, video));
        }
        finish();
    }
}
