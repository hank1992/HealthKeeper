package tw.edu.bmilab.healthkeeper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLdata extends SQLiteOpenHelper {
    private final static String DB = "DB2019.db";
    private final static String TB = "Drug";//table
    private final static int VS = 1;//版本

    public SQLdata(Context context) {
        //super(context, name, factory, version);
        super(context, DB, null, VS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL = "CREATE TABLE IF NOT EXISTS  " + TB + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,Timestamp TEXT NOT NULL, Amount INTEGER, Sex INTEGER)";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL = "DROP TABLE " + TB;
        db.execSQL(SQL);

    }
}
