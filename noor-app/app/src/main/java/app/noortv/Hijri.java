package app.noortv;

import android.content.Context;
import android.icu.util.IslamicCalendar;

/**
 * Today's Hijri date: Umm al-Qura as the starting point, moved by the family's ±1/±2 day setting, because the
 * month begins with the moon sighting the marja announces and that can be a day apart.
 */
final class Hijri {
    static final String[] MONTHS = {"محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى",
            "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"};

    final int day, month, year, monthLength; // month 1 to 12

    private Hijri(int day, int month, int year, int monthLength) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.monthLength = monthLength;
    }

    static Hijri today(Context c) {
        IslamicCalendar h = new IslamicCalendar();
        h.setCalculationType(IslamicCalendar.CalculationType.ISLAMIC_UMALQURA);
        h.add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(c));
        return new Hijri(h.get(IslamicCalendar.DAY_OF_MONTH), h.get(IslamicCalendar.MONTH) + 1,
                h.get(IslamicCalendar.EXTENDED_YEAR), h.getActualMaximum(IslamicCalendar.DAY_OF_MONTH));
    }

    @Override
    public String toString() {
        return day + " " + MONTHS[month - 1] + " " + year;
    }
}
