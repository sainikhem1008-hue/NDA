package com.yourname.nightdutycalculator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "nightduty.db";
    private static final int DATABASE_VERSION = 1;

    // Table for leaves
    public static final String TABLE_LEAVES = "leaves";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DAYS = "days";
    public static final String COLUMN_TYPE = "type";

    // Table for duty records (you can expand later if needed)
    public static final String TABLE_DUTY = "duty";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Leaves table
        String CREATE_LEAVES_TABLE = "CREATE TABLE " + TABLE_LEAVES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DAYS + " INTEGER, "
                + COLUMN_TYPE + " TEXT)";
        db.execSQL(CREATE_LEAVES_TABLE);

        // (Optional) Duty table
        String CREATE_DUTY_TABLE = "CREATE TABLE " + TABLE_DUTY + " (id INTEGER PRIMARY KEY AUTOINCREMENT)";
        db.execSQL(CREATE_DUTY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEAVES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DUTY);
        onCreate(db);
    }
}
