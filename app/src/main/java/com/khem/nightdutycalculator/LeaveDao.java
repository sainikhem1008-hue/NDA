package com.khem.nightdutycalculator;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LeaveDao {

    @Insert
    void insert(LeaveRecord leaveRecord);

    @Update
    void update(LeaveRecord leaveRecord);

    @Delete
    void delete(LeaveRecord leaveRecord);

    @Query("SELECT * FROM leave_records ORDER BY date DESC")
    List<LeaveRecord> getAllLeaveRecords();
}
