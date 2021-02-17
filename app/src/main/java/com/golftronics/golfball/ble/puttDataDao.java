package com.golftronics.golfball.ble;

import android.provider.ContactsContract;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface puttDataDao {

    @Insert
    void insert (PuttData puttsegment);

    @Update
    void update (PuttData puttsegment);

    @Delete
    void delete (PuttData puttsegment);

    @Query("DELETE FROM puttdata_table")
    void deleteAllPutts();


    /* the below query returns all rows/columns but can be modified for a custom query search later
    or another type of query can be added underneath*/



    @Query("SELECT COUNT(*) FROM puttdata_table WHERE distance > 10")
    //LiveData<List<PuttData>> getAllPutts();

    LiveData<Integer> getAllLongPutts();

    //int getAllLongPutts();

}
