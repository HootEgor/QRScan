package ua.com.programmer.barcodetest;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class CursorHelper {

    private final ContentValues values;

    CursorHelper (Cursor cursor){
        int i;
        String[] columns = cursor.getColumnNames();
        values = new ContentValues();
        try {
            for (String column : columns) {
                i = cursor.getColumnIndex(column);
                if (column.equals("_id")) values.put("raw_id", cursor.getLong(i));
                else
                    switch (cursor.getType(i)) {
                        case Cursor.FIELD_TYPE_FLOAT:
                            values.put(column, cursor.getDouble(i));
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            values.put(column, cursor.getLong(i));
                            break;
                        default:
                            values.put(column, cursor.getString(i));
                    }
            }
        }catch (Exception e){
            Log.e("XBUG", "Cursor helper init: "+e);
        }
    }

    int getInt(String column) {
        int value = 0;
        if (column != null) {
            if (values.containsKey(column) && values.get(column) != null){
                if (!values.getAsString(column).isEmpty()) value = values.getAsInteger(column);
            }
        }
        return value;
    }

    long getLong(String column) {
        long value = 0;
        if (column != null) {
            if (values.containsKey(column) && values.get(column) != null){
                if (!values.getAsString(column).isEmpty()) value = values.getAsLong(column);
            }
        }
        return value;
    }

    String getString(String column) {
        String value = "";
        if (column != null) {
            if (values.containsKey(column) && values.get(column) != null){
                value = values.getAsString(column);
            }
        }
        return value;
    }
}
