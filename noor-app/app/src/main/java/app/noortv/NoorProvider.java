package app.noortv;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * Hands the home screen its line under the clock, so prayer times, the Hijri date and the occasions are worked out
 * in one place with the family's settings: content://app.noortv.kahf.noor/line, one row of
 * {hijri, greeting, tone, next}. tone is 1 for gold (birth, feast, White Days), -1 for a mourning day, 0 otherwise.
 * Read-only and holds nothing private.
 */
public class NoorProvider extends ContentProvider {
    static final String[] COLUMNS = {"hijri", "greeting", "tone", "next"};

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
        MatrixCursor c = new MatrixCursor(COLUMNS, 1);
        Hijri h = Hijri.today(getContext());
        Occasions.Line g = Occasions.greeting(h);
        int tone = g == null ? 0 : g.gold ? 1 : Occasions.on(h) != null ? -1 : 0;
        c.addRow(new Object[]{h.toString(), g == null ? "" : g.text, tone, Times.nextLine(getContext())});
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.item/vnd.noortv.line";
    }

    @Override
    public Uri insert(Uri uri, ContentValues v) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] a) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues v, String s, String[] a) {
        return 0;
    }
}
