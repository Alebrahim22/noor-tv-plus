package app.noortv;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

/** What the adhan and the duas share: exact alarms, what is on screen, and the banner over it. */
final class Tv {
    static final String HOME = "com.gathin.tvhome";

    private Tv() {
    }

    /**
     * An exact alarm when Android allows it (USE_EXACT_ALARM, or SCHEDULE_EXACT_ALARM granted over adb), otherwise
     * the closest inexact one, so a missing permission makes the adhan late by a little instead of silent.
     */
    static void alarm(Context c, long when, PendingIntent pi) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        boolean exact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms();
        try {
            if (exact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
        }
    }

    static boolean screenOn(Context c) {
        return c.getSystemService(PowerManager.class).isInteractive();
    }

    static boolean soundPlaying(Context c) {
        return c.getSystemService(AudioManager.class).isMusicActive();
    }

    /** The app in front, from usage events (GET_USAGE_STATS is granted over adb), or null if unknown. */
    static String topApp(Context c) {
        UsageStatsManager usm = c.getSystemService(UsageStatsManager.class);
        long now = System.currentTimeMillis();
        UsageEvents ev = usm.queryEvents(now - 6 * 3600_000L, now);
        UsageEvents.Event e = new UsageEvents.Event();
        String top = null;
        while (ev.hasNextEvent()) {
            ev.getNextEvent(e);
            if (e.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) top = e.getPackageName();
        }
        return top;
    }

    /** The home screen is in front and nothing is playing. */
    static boolean homeIdle(Context c) {
        return HOME.equals(topApp(c)) && !soundPlaying(c);
    }

    /** A rounded banner at the top of the screen, under the clock, for ms milliseconds. */
    static void banner(Context c, String text, int ms) {
        if (!Settings.canDrawOverlays(c)) return;
        Context app = c.getApplicationContext();
        new Handler(Looper.getMainLooper()).post(() -> {
            WindowManager wm = app.getSystemService(WindowManager.class);
            TextView v = new TextView(app);
            v.setText(text);
            v.setTextColor(Color.WHITE);
            v.setTextSize(30);
            v.setTypeface(Typeface.DEFAULT_BOLD);
            v.setPadding(56, 24, 56, 24);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xE60F3D2E);
            bg.setCornerRadius(48);
            bg.setStroke(3, 0xFFFFE08A);
            v.setBackground(bg);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            lp.y = 215; // below the clock block, so the line under it stays readable
            try {
                wm.addView(v, lp);
            } catch (Exception e) {
                return;
            }
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    wm.removeView(v);
                } catch (Exception ignored) {
                }
            }, ms);
        });
    }
}
