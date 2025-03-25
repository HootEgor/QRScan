package energy.h2plt.qrscan

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal class DBHelper(context: Context?) :
    SQLiteOpenHelper(context, "qrData", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "create table history(" +
                    "_id integer primary key autoincrement," +
                    "date text," +
                    "time integer," +
                    "codeType integer," +
                    "codeValue text," +
                    "note text);"
        )
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, i1: Int) {
        if (oldVersion <= 2) {
            sqLiteDatabase.execSQL("alter table history add column time integer")
        }
    }
}
