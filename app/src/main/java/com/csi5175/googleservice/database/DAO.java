package com.csi5175.googleservice.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface DAO {
    @Query("SELECT * FROM useractivitystate")
    List<UserActivityState> getAll();

    @Query("SELECT * FROM useractivitystate WHERE uid IN (:userIds)")
    List<UserActivityState> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM useractivitystate WHERE UID LIKE :uid AND "
            + "TIME LIKE :time LIMIT 1")
    UserActivityState findByType(String uid, String time);

    @Query("SELECT * FROM useractivitystate ORDER BY uid DESC LIMIT 1")
    UserActivityState findLastId();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserActivityState... uas);

    @Delete
    void delete(UserActivityState user);
}
