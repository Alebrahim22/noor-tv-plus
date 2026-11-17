package app.noortv;

import android.content.Context;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Today's Jafari times for the city in gradle.properties, with the family's minute offsets applied. Three adhans
 * a day: Fajr, Dhuhrain (Dhuhr and Asr together, at Dhuhr) and Isha'ain (Maghrib and Isha together, at Maghrib).
 */
final class Times {
    static final int FAJR = 0, DHUHRAIN = 1, ISHAAIN = 2;
    static final String[] NAMES = {"الفجر", "الظهرين", "العشاءين"};

    private Times() {
    }

    /**
     * Minutes after midnight on that day: {Fajr, sunrise, Dhuhr, sunset, Maghrib, Isha}, offsets applied to Fajr,
     * Dhuhr, Maghrib and Isha.
     */
    static int[] day(Context c, Calendar day) {
        double tz = TimeZone.getDefault().getOffset(day.getTimeInMillis()) / 3_600_000.0;
        int[] t = new PrayerCalc(BuildConfig.LAT, BuildConfig.LON)
                .compute(day.get(Calendar.YEAR), day.get(Calendar.MONTH) + 1, day.get(Calendar.DAY_OF_MONTH), tz);
        t[PrayerCalc.FAJR] += Prefs.offset(c, 0);
        t[PrayerCalc.DHUHR] += Prefs.offset(c, 1);
        t[PrayerCalc.MAGHRIB] += Prefs.offset(c, 2);
        t[PrayerCalc.ISHA] += Prefs.offset(c, 3);
        return t;
    }

    /** The three adhan times on that day, in minutes after midnight. */
    static int[] adhans(Context c, Calendar day) {
        int[] t = day(c, day);
        return new int[]{t[PrayerCalc.FAJR], t[PrayerCalc.DHUHR], t[PrayerCalc.MAGHRIB]};
    }

    /** {epoch millis, which} of the next adhan more than 30 seconds away. Always found: the next day has three. */
    static long[] next(Context c) {
        Calendar now = Calendar.getInstance();
        for (int offset = 0; offset < 3; offset++) {
            Calendar day = (Calendar) now.clone();
            day.add(Calendar.DAY_OF_YEAR, offset);
            int[] a = adhans(c, day);
            for (int i = 0; i < a.length; i++) {
                Calendar at = atMinutes(day, a[i]);
                if (at.getTimeInMillis() > now.getTimeInMillis() + 30_000) return new long[]{at.getTimeInMillis(), i};
            }
        }
        throw new IllegalStateException("no adhan in three days");
    }

    /** "🕌 الظهرين بعد 1:12" for the line under the clock. */
    static String nextLine(Context c) {
        long[] n = next(c);
        long mins = Math.max(0, (n[0] - System.currentTimeMillis() + 59_999) / 60_000);
        return "🕌 " + NAMES[(int) n[1]] + " بعد " + String.format(Locale.US, "%d:%02d", mins / 60, mins % 60);
    }

    static Calendar atMinutes(Calendar day, int minutes) {
        Calendar at = (Calendar) day.clone();
        at.set(Calendar.HOUR_OF_DAY, 0);
        at.set(Calendar.MINUTE, 0);
        at.set(Calendar.SECOND, 0);
        at.set(Calendar.MILLISECOND, 0);
        at.add(Calendar.MINUTE, minutes);
        return at;
    }

    static String hm(int minutes) {
        minutes = ((minutes % 1440) + 1440) % 1440;
        int h = minutes / 60, m = minutes % 60;
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return String.format(Locale.US, "%d:%02d %s", h12, m, h < 12 ? "ص" : "م");
    }
}
