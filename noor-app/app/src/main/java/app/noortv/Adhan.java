package app.noortv;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

/**
 * The adhan three times a day at the Jafari times (Fajr, Dhuhrain, Isha'ain), from res/raw/adhan, with no iqama
 * and no reminder before or after. With the home screen up and nothing playing it plays under a banner; while a
 * film, a channel or HDMI is on it only shows the banner, so nothing is cut off. If a dua is playing, the adhan
 * takes over. The times are worked out on the TV, so the next alarm can always be set, with or without internet.
 */
public class Adhan extends BroadcastReceiver {
    static final String FIRE = "app.noortv.ADHAN";

    private static MediaPlayer player;

    static void schedule(Context c) {
        Context app = c.getApplicationContext();
        long[] next = Times.next(app);
        Intent fire = new Intent(app, Adhan.class).setAction(FIRE).putExtra("p", (int) next[1]);
        PendingIntent pi = PendingIntent.getBroadcast(app, 1, fire,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Tv.alarm(app, next[0], pi);
    }

    @Override
    public void onReceive(Context c, Intent i) {
        if (!BuildConfig.REMINDERS || !FIRE.equals(i.getAction())) return;
        Context app = c.getApplicationContext();
        int prayer = i.getIntExtra("p", Times.DHUHRAIN);
        schedule(app);
        if (!Tv.screenOn(app)) return; // TV is off
        boolean duaOn = DuaActivity.isPlaying();
        boolean idle = duaOn || Tv.homeIdle(app);
        if (duaOn) DuaActivity.stopNow();
        fire(app, prayer, idle);
    }

    /** Banner always; the adhan itself only when play is true. */
    static void fire(Context app, int prayer, boolean play) {
        String text = "🕌 حان الآن موعد صلاة " + Times.NAMES[prayer];
        int raw = app.getResources().getIdentifier("adhan", "raw", app.getPackageName());
        if (!play || raw == 0) {
            Tv.banner(app, text, 30_000);
            return;
        }
        stop();
        player = MediaPlayer.create(app, raw, new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                app.getSystemService(AudioManager.class).generateAudioSessionId());
        if (player == null) {
            Tv.banner(app, text, 30_000);
            return;
        }
        player.setOnCompletionListener(mp -> stop());
        player.start();
        Tv.banner(app, text, player.getDuration());
        watch(app);
    }

    /** Stops the adhan as soon as an app other than the home screen comes to the front. */
    private static void watch(Context app) {
        Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player == null) return;
                String top = Tv.topApp(app);
                if (top != null && !Tv.HOME.equals(top) && !app.getPackageName().equals(top)) {
                    stop();
                    return;
                }
                h.postDelayed(this, 1500);
            }
        }, 3000);
    }

    static void stop() {
        if (player == null) return;
        try {
            player.stop();
        } catch (Exception ignored) {
        }
        player.release();
        player = null;
    }
}
