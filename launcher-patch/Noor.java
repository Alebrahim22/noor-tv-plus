package com.gathin.tvhome;

import android.content.Context;
import android.database.Cursor;
import android.icu.util.IslamicCalendar;
import android.net.Uri;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The Noor line on the home screen. Above the clock: the day's Jafari occasion, the White Days, or «جمعة مباركة».
 * Under it: the Hijri date, the weather, and the time left to the next adhan (Fajr, Dhuhrain, Isha'ain).
 *
 * Everything but the weather comes from the Kahf app (content://app.noortv.kahf.noor/line), which works the
 * times out on the TV with the family's settings from the phone page. The weather comes from Open-Meteo.
 * Without the Kahf app, the line falls back to the Umm al-Qura date alone.
 */
final class Noor {
    static final double LAT = @LAT@, LON = @LON@;
    private static final Uri LINE = Uri.parse("content://app.noortv.kahf.noor/line");
    private static final String[] MONTHS = {"محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى",
            "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"};

    private static String hijri = "", greeting = "", next = "";
    private static int tone; // 1 gold, -1 mourning, 0 plain
    private static long readAt;

    private static volatile String weather = "";
    private static volatile long weatherAt;
    private static volatile boolean fetching;

    private Noor() {
    }

    private static void read(Context c) {
        long now = System.currentTimeMillis();
        if (now - readAt < 20_000) return;
        readAt = now;
        try (Cursor k = c.getContentResolver().query(LINE, null, null, null, null)) {
            if (k != null && k.moveToFirst()) {
                hijri = k.getString(0);
                greeting = k.getString(1);
                tone = k.getInt(2);
                next = k.getString(3);
                return;
            }
        } catch (Exception ignored) {
        }
        IslamicCalendar h = new IslamicCalendar();
        h.setCalculationType(IslamicCalendar.CalculationType.ISLAMIC_UMALQURA);
        hijri = h.get(IslamicCalendar.DAY_OF_MONTH) + " " + MONTHS[h.get(IslamicCalendar.MONTH)] + " "
                + h.get(IslamicCalendar.EXTENDED_YEAR);
        greeting = "";
        tone = 0;
        next = "";
    }

    /** The line above the clock, or null for the launcher's own greeting. */
    static String greeting(Context c) {
        read(c);
        return greeting.isEmpty() ? null : greeting;
    }

    static String line(Context c) {
        read(c);
        refreshWeather();
        StringBuilder b = new StringBuilder(hijri);
        if (!weather.isEmpty()) b.append("  ·  ").append(weather);
        if (!next.isEmpty()) b.append('\n').append(next);
        return b.toString();
    }

    /** Gold on births, feasts and the White Days; a calm grey on mourning days; white otherwise. */
    static int color(Context c) {
        read(c);
        return tone > 0 ? 0xFFFFE08A : tone < 0 ? 0xB3D0D4D8 : 0xE6FFFFFF;
    }

    private static void refreshWeather() {
        if (fetching || System.currentTimeMillis() - weatherAt < 30 * 60_000L) return;
        fetching = true;
        new Thread(() -> {
            try {
                JSONObject cur = new JSONObject(get("https://api.open-meteo.com/v1/forecast?latitude=" + LAT
                        + "&longitude=" + LON + "&current=temperature_2m,weather_code,is_day&timezone=auto"))
                        .getJSONObject("current");
                long t = Math.round(cur.getDouble("temperature_2m"));
                weather = icon(cur.getInt("weather_code"), cur.optInt("is_day", 1) == 1) + " " + t + "°";
                weatherAt = System.currentTimeMillis();
            } catch (Exception ignored) {
                weatherAt = System.currentTimeMillis() - 25 * 60_000L; // try again in 5 minutes
            } finally {
                fetching = false;
            }
        }, "noor-weather").start();
    }

    /** WMO weather code to one symbol. */
    private static String icon(int code, boolean day) {
        if (code == 0) return day ? "☀️" : "🌙";
        if (code <= 2) return day ? "🌤️" : "🌙";
        if (code == 3) return "☁️";
        if (code == 45 || code == 48) return "🌫️";
        if (code >= 95) return "⛈️";
        if (code >= 51) return "🌧️";
        return "🌤️";
    }

    private static String get(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        try (InputStream in = c.getInputStream()) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
            return b.toString("UTF-8");
        } finally {
            c.disconnect();
        }
    }
}
