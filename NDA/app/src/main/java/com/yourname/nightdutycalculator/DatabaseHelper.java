package com.yourname.nightdutycalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "night_duty.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_NAME = "duty_records";

    // Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_TOTAL_HOURS = "total_hours";
    private static final String COLUMN_NIGHT_HOURS = "night_hours";
    private static final String COLUMN_BASIC_PAY = "basic_pay";
    private static final String COLUMN_DA = "dearness_allowance";
    private static final String COLUMN_NDA = "nda_amount";
    private static final String COLUMN_CR = "cr";
    private static final String COLUMN_TYPE = "duty_type"; // Morning/Evening/Night/Weekly Rest

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_START_TIME + " TEXT, "
                + COLUMN_END_TIME + " TEXT, "
                + COLUMN_TOTAL_HOURS + " REAL, "
                + COLUMN_NIGHT_HOURS + " REAL, "
                + COLUMN_BASIC_PAY + " REAL, "
                + COLUMN_DA + " REAL, "
                + COLUMN_NDA + " REAL, "
                + COLUMN_CR + " TEXT, "
                + COLUMN_TYPE + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert new duty record
    public boolean insertDuty(String date, String startTime, String endTime,
                              double totalHours, double nightHours,
                              double basicPay, double da, double ndaAmount,
                              String cr, String dutyType) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_START_TIME, startTime);
        values.put(COLUMN_END_TIME, endTime);
        values.put(COLUMN_TOTAL_HOURS, totalHours);
        values.put(COLUMN_NIGHT_HOURS, nightHours);
        values.put(COLUMN_BASIC_PAY, basicPay);
        values.put(COLUMN_DA, da);
        values.put(COLUMN_NDA, ndaAmount);
        values.put(COLUMN_CR, cr);
        values.put(COLUMN_TYPE, dutyType);

        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result != -1;
    }

    // Fetch all duty records
    public Cursor getAllDuties() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC", null);
    }

    // Delete record by ID
    public void deleteDuty(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Update record
    public void updateDuty(int id, String date, String startTime, String endTime,
                           double totalHours, double nightHours,
                           double basicPay, double da, double ndaAmount,
                           String cr, String dutyType) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_START_TIME, startTime);
        values.put(COLUMN_END_TIME, endTime);
        values.put(COLUMN_TOTAL_HOURS, totalHours);
        values.put(COLUMN_NIGHT_HOURS, nightHours);
        values.put(COLUMN_BASIC_PAY, basicPay);
        values.put(COLUMN_DA, da);
        values.put(COLUMN_NDA, ndaAmount);
        values.put(COLUMN_CR, cr);
        values.put(COLUMN_TYPE, dutyType);

        db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
}
