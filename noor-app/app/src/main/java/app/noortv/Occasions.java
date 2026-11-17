package app.noortv;

import java.util.Calendar;

/**
 * The line above the clock on Jafari occasions, by the adjusted Hijri date. Births and feasts are gold; martyrdoms,
 * deaths and the first ten days of Muharram are a calm grey line with no greeting, and «جمعة مباركة» is not shown
 * on those days. Where narrations differ, the date most kept in Kuwait is used (see occasions-jafari.json).
 */
final class Occasions {
    /** Text for the line plus its colour. */
    static final class Line {
        final String text;
        final boolean gold;

        Line(String text, boolean gold) {
            this.text = text;
            this.gold = gold;
        }
    }

    private static final int LAST = 0; // "the last day of the month"
    private static final boolean G = true, M = false; // gold, mourning

    // {month, from day, to day}, gold, text. "{n}" becomes the day of the month.
    private static final Object[][] TABLE = {
            {1, 1, 9, M, "عشرة محرم · اليوم {n}"},
            {1, 10, 10, M, "عاشوراء · ذكرى استشهاد الإمام الحسين (ع)"},
            {1, 25, 25, M, "ذكرى استشهاد الإمام زين العابدين (ع)"},
            {2, 7, 7, M, "ذكرى استشهاد الإمام الحسن المجتبى (ع)"},
            {2, 20, 20, M, "أربعينية الإمام الحسين (ع)"},
            {2, 28, 28, M, "ذكرى وفاة النبي محمد ﷺ"},
            {2, LAST, LAST, M, "ذكرى استشهاد الإمام الرضا (ع)"},
            {3, 8, 8, M, "ذكرى استشهاد الإمام الحسن العسكري (ع)"},
            {3, 9, 9, G, "بداية إمامة الإمام المهدي (عج)"},
            {3, 17, 17, G, "ميلاد النبي محمد ﷺ والإمام الصادق (ع)"},
            {4, 8, 8, G, "ميلاد الإمام الحسن العسكري (ع)"},
            {5, 5, 5, G, "ميلاد السيدة زينب (ع)"},
            {5, 13, 13, M, "الفاطمية الأولى · ذكرى استشهاد السيدة الزهراء (ع)"},
            {6, 3, 3, M, "الفاطمية الثانية · ذكرى استشهاد السيدة الزهراء (ع)"},
            {6, 20, 20, G, "ميلاد السيدة فاطمة الزهراء (ع)"},
            {7, 1, 1, G, "ميلاد الإمام الباقر (ع)"},
            {7, 3, 3, M, "ذكرى استشهاد الإمام الهادي (ع)"},
            {7, 10, 10, G, "ميلاد الإمام الجواد (ع)"},
            {7, 13, 13, G, "ميلاد الإمام علي (ع)"},
            {7, 15, 15, M, "ذكرى وفاة السيدة زينب (ع)"},
            {7, 25, 25, M, "ذكرى استشهاد الإمام الكاظم (ع)"},
            {7, 27, 27, G, "المبعث النبوي الشريف"},
            {8, 3, 3, G, "ميلاد الإمام الحسين (ع)"},
            {8, 4, 4, G, "ميلاد أبي الفضل العباس (ع)"},
            {8, 5, 5, G, "ميلاد الإمام زين العابدين (ع)"},
            {8, 11, 11, G, "ميلاد علي الأكبر (ع)"},
            {8, 15, 15, G, "ميلاد الإمام المهدي (عج)"},
            {9, 1, 1, G, "شهر رمضان المبارك"},
            {9, 15, 15, G, "ميلاد الإمام الحسن المجتبى (ع)"},
            {9, 19, 19, M, "ليلة القدر · ذكرى ضربة الإمام علي (ع)"},
            {9, 21, 21, M, "ليلة القدر · ذكرى استشهاد الإمام علي (ع)"},
            {9, 23, 23, G, "ليلة القدر"},
            {10, 1, 1, G, "عيد الفطر المبارك"},
            {10, 25, 25, M, "ذكرى استشهاد الإمام الصادق (ع)"},
            {11, 1, 1, G, "ميلاد السيدة فاطمة المعصومة (ع)"},
            {11, 11, 11, G, "ميلاد الإمام الرضا (ع)"},
            {11, LAST, LAST, M, "ذكرى استشهاد الإمام الجواد (ع)"},
            {12, 7, 7, M, "ذكرى استشهاد الإمام الباقر (ع)"},
            {12, 9, 9, G, "يوم عرفة"},
            {12, 10, 10, G, "عيد الأضحى المبارك"},
            {12, 15, 15, G, "ميلاد الإمام الهادي (ع)"},
            {12, 18, 18, G, "عيد الغدير الأغر"},
            {12, 24, 24, G, "يوم المباهلة"},
    };

    private Occasions() {
    }

    /** Today's occasion, or null. */
    static Line on(Hijri h) {
        for (Object[] row : TABLE) {
            int m = (int) row[0], from = (int) row[1], to = (int) row[2];
            if (m != h.month) continue;
            boolean hit = from == LAST ? h.day == h.monthLength : h.day >= from && h.day <= to;
            if (hit) return new Line(((String) row[4]).replace("{n}", String.valueOf(h.day)), (boolean) row[3]);
        }
        return null;
    }

    /**
     * The line above the clock: an occasion first; then the White Days (13 to 15); then «جمعة مباركة» on a plain
     * Friday. Null when there is nothing to say.
     */
    static Line greeting(Hijri h) {
        Line o = on(h);
        if (o != null) return o;
        if (h.day >= 13 && h.day <= 15) return new Line("الأيام البيض 🤍 اليوم " + h.day, true);
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) return new Line("جمعة مباركة 🌿", false);
        return null;
    }
}
