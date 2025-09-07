package com.yourname.nightdutycalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "night_duty.db";
    private static final int DATABASE_VERSION = 2;

    // Duty Table
    private static final String TABLE_DUTY = "duty_records";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_START = "start_time";
    private static final String COLUMN_END = "end_time";
    private static final String COLUMN_TOTAL = "total_hours";
    private static final String COLUMN_NIGHT = "night_hours";

    // Leave Table
    private static final String TABLE_LEAVE = "leave_records";
    private static final String COLUMN_LEAVE_ID = "leave_id";
    private static final String COLUMN_LEAVE_FROM = "leave_from";
    private static final String COLUMN_LEAVE_TO = "leave_to";
    private static final String COLUMN_LEAVE_TYPE = "leave_type";
    private static final String COLUMN_APPLIED_DATE = "applied_date";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_NOTES = "notes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // ✅ Create Duty Table
        String createDutyTable = "CREATE TABLE " + TABLE_DUTY + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_START + " TEXT,"
                + COLUMN_END + " TEXT,"
                + COLUMN_TOTAL + " REAL,"
                + COLUMN_NIGHT + " REAL"
                + ")";
        db.execSQL(createDutyTable);

        // ✅ Create Leave Table
        String createLeaveTable = "CREATE TABLE " + TABLE_LEAVE + "("
                + COLUMN_LEAVE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_LEAVE_FROM + " TEXT,"
                + COLUMN_LEAVE_TO + " TEXT,"
                + COLUMN_LEAVE_TYPE + " TEXT,"
                + COLUMN_APPLIED_DATE + " TEXT,"
                + COLUMN_STATUS + " TEXT,"
                + COLUMN_NOTES + " TEXT"
                + ")";
        db.execSQL(createLeaveTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DUTY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEAVE);
        onCreate(db);
    }

    // ✅ Insert Duty Record
    public void insertDutyRecord(DutyRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, record.getDate());
        values.put(COLUMN_START, record.getStartTime());
        values.put(COLUMN_END, record.getEndTime());
        values.put(COLUMN_TOTAL, record.getTotalHours());
        values.put(COLUMN_NIGHT, record.getNightHours());
        db.insert(TABLE_DUTY, null, values);
        db.close();
    }

    // ✅ Fetch Duty Records
    public List<DutyRecord> getAllDutyRecords() {
        List<DutyRecord> dutyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DUTY, null);

        if (cursor.moveToFirst()) {
            do {
                DutyRecord record = new DutyRecord();
                record.setId(cursor.getInt(0));
                record.setDate(cursor.getString(1));
                record.setStartTime(cursor.getString(2));
                record.setEndTime(cursor.getString(3));
                record.setTotalHours(cursor.getDouble(4));
                record.setNightHours(cursor.getDouble(5));
                dutyList.add(record);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return dutyList;
    }

    // ✅ Insert Leave Record
    public void insertLeaveRecord(LeaveRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LEAVE_FROM, record.getLeaveFrom());
        values.put(COLUMN_LEAVE_TO, record.getLeaveTo());
        values.put(COLUMN_LEAVE_TYPE, record.getLeaveType());
        values.put(COLUMN_APPLIED_DATE, record.getAppliedDate());
        values.put(COLUMN_STATUS, record.getStatus());
        values.put(COLUMN_NOTES, record.getNotes());
        db.insert(TABLE_LEAVE, null, values);
        db.close();
    }

    // ✅ Fetch Leave Records
    public List<LeaveRecord> getAllLeaveRecords() {
        List<LeaveRecord> leaveList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LEAVE, null);

        if (cursor.moveToFirst()) {
            do {
                LeaveRecord record = new LeaveRecord();
                record.setId(cursor.getLong(0));
                record.setLeaveFrom(cursor.getString(1));
                record.setLeaveTo(cursor.getString(2));
                record.setLeaveType(cursor.getString(3));
                record.setAppliedDate(cursor.getString(4));
                record.setStatus(cursor.getString(5));
                record.setNotes(cursor.getString(6));
                leaveList.add(record);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return leaveList;
    }
}
