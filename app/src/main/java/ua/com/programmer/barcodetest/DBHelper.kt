package ua.com.programmer.barcodetest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {

    DBHelper (Context context){
        super(context,"qrData",null,2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table history(" +
                "_id integer primary key autoincrement," +
                "date text," +
                "time integer," +
                "codeType integer," +
                "codeValue text," +
                "note text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int i1) {
        if (oldVersion<=2){
            sqLiteDatabase.execSQL("alter table history add column time integer");
        }
    }
}
