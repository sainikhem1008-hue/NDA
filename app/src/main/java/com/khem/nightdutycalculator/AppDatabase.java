package com.khem.nightdutycalculator;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DutyRecord.class, LeaveRecord.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DutyDao dutyDao();
    public abstract LeaveDao leaveDao();
}
