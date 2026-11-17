package app.noortv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * Plays a dua full screen: its name, the reciter and the time left over a dark background. Any remote button
 * stops it and goes back. Opened by its card, or on its own at the dua's weekly time.
 */
public class DuaActivity extends Activity {
    private static DuaActivity current;

    private MediaPlayer player;
    private TextView left;
    private final Handler tick = new Handler(Looper.getMainLooper());

    /** Opens the dua in the Kahf app, which carries the recordings. False if Android refused to open it. */
    static boolean start(Context c, Duas.Dua d) {
        Intent i = new Intent().setClassName("app.noortv.kahf", DuaActivity.class.getName())
                .putExtra("dua", d.key)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            c.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean isPlaying() {
        DuaActivity a = current;
        return a != null && a.player != null && a.player.isPlaying();
    }

    /** Called by the adhan: the dua gives way. */
    static void stopNow() {
        DuaActivity a = current;
        if (a != null) a.runOnUiThread(a::finish);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Duas.Dua d = Duas.byKey(getIntent().getStringExtra("dua"));
        int raw = d == null ? 0 : getResources().getIdentifier(d.key, "raw", getPackageName());
        if (raw == 0) {
            finish();
            return;
        }
        Adhan.stop();
        setContentView(layout(d));
        player = MediaPlayer.create(this, raw, new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build(), 0);
        if (player == null) {
            finish();
            return;
        }
        player.setOnCompletionListener(mp -> finish());
        player.start();
        current = this;
        tick.post(new Runnable() {
            @Override
            public void run() {
                if (player == null) return;
                try {
                    int s = (player.getDuration() - player.getCurrentPosition()) / 1000;
                    left.setText(String.format(Locale.US, "باقي %d:%02d · اضغط أي زر للإيقاف", s / 60, s % 60));
                } catch (IllegalStateException ignored) {
                }
                tick.postDelayed(this, 1000);
            }
        });
    }

    private LinearLayout layout(Duas.Dua d) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{0xFF0B1A2A, 0xFF22163A});
        root.setBackground(bg);
        root.addView(text("📿", 64, Color.WHITE, false));
        root.addView(text(d.title, 72, Color.WHITE, true));
        root.addView(text(d.reciter, 34, 0xFFFFE08A, false));
        left = text("", 24, 0xB3FFFFFF, false);
        left.setPadding(0, 60, 0, 0);
        root.addView(left);
        return root;
    }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            return super.onKeyDown(keyCode, e); // let the volume keys work
        }
        finish();
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish(); // Home or another app took over: stop the sound with it
    }

    @Override
    protected void onDestroy() {
        tick.removeCallbacksAndMessages(null);
        if (player != null) {
            try {
                player.stop();
            } catch (Exception ignored) {
            }
            player.release();
            player = null;
        }
        if (current == this) current = null;
        super.onDestroy();
    }
}
