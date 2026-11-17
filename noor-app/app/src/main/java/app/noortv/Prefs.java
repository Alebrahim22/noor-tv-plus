package app.noortv;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Everything the family can change from the phone page, kept on the TV: minutes added to each prayer time (to
 * match the local mosque's timetable), days added to the Hijri date (moon sighting), and the two duas' times.
 */
final class Prefs {
    /** Keys of the per-prayer minute offsets, in the order of {@link Times#NAMES} plus Isha (shown, no adhan). */
    static final String[] OFFSET_KEYS = {"off_fajr", "off_dhuhr", "off_maghrib", "off_isha"};
    static final int MAX_OFFSET = 30, MAX_HIJRI = 2;

    private Prefs() {
    }

    static SharedPreferences of(Context c) {
        return c.getApplicationContext().getSharedPreferences("noor", Context.MODE_PRIVATE);
    }

    static int offset(Context c, int i) {
        return of(c).getInt(OFFSET_KEYS[i], 0);
    }

    static int hijriOffset(Context c) {
        return of(c).getInt("hijri", 0);
    }

    static boolean duaOn(Context c, Duas.Dua d) {
        return of(c).getBoolean(d.key + "_on", true);
    }

    /** Minutes after midnight. */
    static int duaTime(Context c, Duas.Dua d) {
        return of(c).getInt(d.key + "_time", d.defaultMinutes);
    }

    /**
     * Applies one "key=value" from the phone page. Only known keys within their range are accepted.
     * Returns false for anything else.
     */
    static boolean set(Context c, String key, String value) {
        SharedPreferences.Editor e = of(c).edit();
        try {
            if (key.equals("hijri")) {
                e.putInt(key, clamp(Integer.parseInt(value), MAX_HIJRI));
            } else if (key.startsWith("off_") && indexOf(key) >= 0) {
                e.putInt(key, clamp(Integer.parseInt(value), MAX_OFFSET));
            } else if (key.endsWith("_on") && Duas.byKey(key.substring(0, key.length() - 3)) != null) {
                e.putBoolean(key, Boolean.parseBoolean(value));
            } else if (key.endsWith("_time") && Duas.byKey(key.substring(0, key.length() - 5)) != null) {
                String[] hm = value.split(":");
                int h = Integer.parseInt(hm[0]), m = Integer.parseInt(hm[1]);
                if (h < 0 || h > 23 || m < 0 || m > 59) return false;
                e.putInt(key, h * 60 + m);
            } else {
                return false;
            }
        } catch (RuntimeException bad) {
            return false;
        }
        e.apply();
        return true;
    }

    private static int indexOf(String key) {
        for (int i = 0; i < OFFSET_KEYS.length; i++) if (OFFSET_KEYS[i].equals(key)) return i;
        return -1;
    }

    private static int clamp(int v, int max) {
        return Math.max(-max, Math.min(max, v));
    }
}
