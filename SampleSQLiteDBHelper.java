package com.vogella.android.smag_btp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by namit on 8/4/17.
 */
public class SampleSQLiteDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "btp_database";
    public static final String TABLE_NAME = "CropData";
    public static final String ID = "_id";
    public static final String COLUMN_DATE = "DateTEXT";
    public static final String COLUMN_TEMPERATURE = "Temperature";
    public static final String COLUMN_MOISTURE = "Moisture";
    public static final String COLUMN_INTRUSION = "Intrusion";

    public SampleSQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TEMPERATURE + " TEXT, " +
                COLUMN_MOISTURE + " TEXT, " +
                COLUMN_INTRUSION + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(Double temp, Double moist, Integer intru) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        contentValues.put(COLUMN_DATE, date);
        contentValues.put(COLUMN_TEMPERATURE, temp);
        contentValues.put(COLUMN_MOISTURE, moist);
        contentValues.put(COLUMN_INTRUSION, intru);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result==-1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + TABLE_NAME, null );
        return res;
    }

    /*public Integer deleteData(String id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[] {id});
    }*/

}
