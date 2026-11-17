package app.noortv;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Weekly duas on their own: Du'a al-Nudba on Friday morning and Du'a Kumayl on Thursday night (times changeable
 * from the phone page). With the home screen up and nothing playing, the dua opens full screen and plays; any
 * remote button stops it. While something else is on, a banner points to its card instead of cutting in.
 */
public class Duas extends BroadcastReceiver {
    static final String FIRE = "app.noortv.DUA";

    enum Dua {
        NUDBA("nudba", "دعاء الندبة", "باقر المقدسي", Calendar.FRIDAY, 10 * 60 + 30, 10),
        KUMAYL("kumayl", "دعاء كميل", "علي بوحمد", Calendar.THURSDAY, 20 * 60 + 30, 11);

        final String key, title, reciter;
        final int weekday, defaultMinutes, requestCode;

        Dua(String key, String title, String reciter, int weekday, int defaultMinutes, int requestCode) {
            this.key = key;
            this.title = title;
            this.reciter = reciter;
            this.weekday = weekday;
            this.defaultMinutes = defaultMinutes;
            this.requestCode = requestCode;
        }
    }

    static Dua byKey(String key) {
        for (Dua d : Dua.values()) if (d.key.equals(key)) return d;
        return null;
    }

    static void schedule(Context c) {
        Context app = c.getApplicationContext();
        for (Dua d : Dua.values()) {
            Intent fire = new Intent(app, Duas.class).setAction(FIRE).putExtra("dua", d.key);
            PendingIntent pi = PendingIntent.getBroadcast(app, d.requestCode, fire,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            if (!Prefs.duaOn(app, d)) {
                pi.cancel();
                continue;
            }
            Tv.alarm(app, next(app, d), pi);
        }
    }

    /** The next weekday at the dua's time, more than a minute away. */
    static long next(Context c, Dua d) {
        Calendar now = Calendar.getInstance();
        for (int i = 0; i < 8; i++) {
            Calendar day = (Calendar) now.clone();
            day.add(Calendar.DAY_OF_YEAR, i);
            if (day.get(Calendar.DAY_OF_WEEK) != d.weekday) continue;
            Calendar at = Times.atMinutes(day, Prefs.duaTime(c, d));
            if (at.getTimeInMillis() > now.getTimeInMillis() + 60_000) return at.getTimeInMillis();
        }
        Calendar at = Times.atMinutes(now, Prefs.duaTime(c, d));
        at.add(Calendar.DAY_OF_YEAR, 7);
        return at.getTimeInMillis();
    }

    @Override
    public void onReceive(Context c, Intent i) {
        if (!BuildConfig.REMINDERS || !FIRE.equals(i.getAction())) return;
        Context app = c.getApplicationContext();
        schedule(app);
        Dua d = byKey(i.getStringExtra("dua"));
        if (d == null || !Prefs.duaOn(app, d) || !Tv.screenOn(app)) return;
        if (Tv.homeIdle(app) && DuaActivity.start(app, d)) return;
        Tv.banner(app, "📿 حان وقت " + d.title + " · افتح كرته من الشريط", 60_000);
    }
}
