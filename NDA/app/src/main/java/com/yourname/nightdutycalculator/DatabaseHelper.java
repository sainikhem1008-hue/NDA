package com.yourname.nightdutycalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "night_duty.db";
    private static final int DB_VER = 1;
    private static final String TABLE = "duty_records";

    public DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "date TEXT, start_time TEXT, end_time TEXT, "
                + "total_hours REAL, night_hours REAL, basic_pay REAL, da REAL, "
                + "nda REAL, holiday_paid INTEGER, allowance_status TEXT, "
                + "weekly_rest INTEGER, weekly_rest_duty INTEGER, dual_duty INTEGER, leave_status TEXT, notes TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // insert with full fields
    public boolean insertDuty(String date, String start, String end,
                              double totalHours, double nightHours,
                              double basicPay, double da, double nda,
                              boolean holidayPaid, String allowanceStatus,
                              boolean weeklyRest, boolean weeklyRestDuty,
                              boolean dualDuty, String leaveStatus, String notes) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("date", date);
            cv.put("start_time", start);
            cv.put("end_time", end);
            cv.put("total_hours", totalHours);
            cv.put("night_hours", nightHours);
            cv.put("basic_pay", basicPay);
            cv.put("da", da);
            cv.put("nda", nda);
            cv.put("holiday_paid", holidayPaid ? 1 : 0);
            cv.put("allowance_status", allowanceStatus);
            cv.put("weekly_rest", weeklyRest ? 1 : 0);
            cv.put("weekly_rest_duty", weeklyRestDuty ? 1 : 0);
            cv.put("dual_duty", dualDuty ? 1 : 0);
            cv.put("leave_status", leaveStatus);
            cv.put("notes", notes);
            long id = db.insert(TABLE, null, cv);
            db.close();
            return id != -1;
        } catch (Exception e) {
            return false;
        }
    }

    public Cursor getAllDuties() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE + " ORDER BY id DESC", null);
    }

    public void deleteDuty(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }
}
