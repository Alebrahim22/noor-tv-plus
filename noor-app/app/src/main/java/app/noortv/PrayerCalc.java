package app.noortv;

/**
 * Jafari prayer times worked out on the TV, with no network: Fajr when the sun is 16° below the horizon,
 * Maghrib at 4° (once the eastern redness has gone), Isha at 14°. These are the Shia Ithna-Ashari (Leva
 * Institute, Qum) angles, the same as Aladhan's method 0. The astronomy follows the public-domain formulas in
 * praytimes.org (sun position from the US Naval Observatory's low-precision equations).
 *
 * Plain Java with no Android classes, so it can be checked on a computer against Aladhan.
 */
public final class PrayerCalc {
    public static final double FAJR_ANGLE = 16, MAGHRIB_ANGLE = 4, ISHA_ANGLE = 14;
    private static final double RISE_SET_ANGLE = 0.833;

    /** Index of each time in the array {@link #compute} returns. */
    public static final int FAJR = 0, SUNRISE = 1, DHUHR = 2, SUNSET = 3, MAGHRIB = 4, ISHA = 5;

    private final double lat, lon;
    private double jDate;

    public PrayerCalc(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Minutes after local midnight, rounded to the nearest minute, for Fajr, sunrise, Dhuhr, sunset, Maghrib and
     * Isha on that day. {@code tzHours} is the zone's offset from UTC on that day (3 for Kuwait).
     */
    public int[] compute(int year, int month, int day, double tzHours) {
        jDate = julian(year, month, day) - lon / (15 * 24.0);
        // Start from rough guesses, then refine twice: each pass uses the sun's position at the previous answer.
        double[] t = {5, 6, 12, 18, 18, 18};
        for (int i = 0; i < 2; i++) t = pass(t);
        int[] out = new int[t.length];
        for (int i = 0; i < t.length; i++) {
            double h = t[i] + tzHours - lon / 15.0;
            out[i] = (int) Math.floor(fixHour(h) * 60 + 0.5) % (24 * 60);
        }
        return out;
    }

    private double[] pass(double[] t) {
        return new double[]{
                sunAngleTime(FAJR_ANGLE, t[FAJR] / 24, true),
                sunAngleTime(RISE_SET_ANGLE, t[SUNRISE] / 24, true),
                midDay(t[DHUHR] / 24),
                sunAngleTime(RISE_SET_ANGLE, t[SUNSET] / 24, false),
                sunAngleTime(MAGHRIB_ANGLE, t[MAGHRIB] / 24, false),
                sunAngleTime(ISHA_ANGLE, t[ISHA] / 24, false),
        };
    }

    private double midDay(double dayFraction) {
        return fixHour(12 - sun(jDate + dayFraction)[1]);
    }

    private double sunAngleTime(double angle, double dayFraction, boolean beforeNoon) {
        double decl = sun(jDate + dayFraction)[0];
        double noon = midDay(dayFraction);
        double cos = (-sin(angle) - sin(decl) * sin(lat)) / (cos(decl) * cos(lat));
        cos = Math.max(-1, Math.min(1, cos)); // far north in summer the sun may never get that low
        double t = Math.toDegrees(Math.acos(cos)) / 15.0;
        return noon + (beforeNoon ? -t : t);
    }

    /** {declination in degrees, equation of time in hours} for a Julian date. */
    private static double[] sun(double jd) {
        double d = jd - 2451545.0;
        double g = fixAngle(357.529 + 0.98560028 * d);
        double q = fixAngle(280.459 + 0.98564736 * d);
        double l = fixAngle(q + 1.915 * sin(g) + 0.020 * sin(2 * g));
        double e = 23.439 - 0.00000036 * d;
        double ra = Math.toDegrees(Math.atan2(cos(e) * sin(l), cos(l))) / 15.0;
        double eqt = q / 15.0 - fixHour(ra);
        double decl = Math.toDegrees(Math.asin(sin(e) * sin(l)));
        return new double[]{decl, eqt};
    }

    private static double julian(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double a = Math.floor(year / 100.0);
        double b = 2 - a + Math.floor(a / 4);
        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + b - 1524.5;
    }

    private static double sin(double deg) {
        return Math.sin(Math.toRadians(deg));
    }

    private static double cos(double deg) {
        return Math.cos(Math.toRadians(deg));
    }

    private static double fixAngle(double a) {
        a = a - 360 * Math.floor(a / 360);
        return a < 0 ? a + 360 : a;
    }

    private static double fixHour(double h) {
        h = h - 24 * Math.floor(h / 24);
        return h < 0 ? h + 24 : h;
    }
}
