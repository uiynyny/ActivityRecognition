/*
 * Yan Zhang
 * 300052103
 * POJO for data base entity, used for ORM with Room
 * */
package com.csi5175.googleservice.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "UserActivityState")
public class UserActivityState {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "TYPE")
    private String type;

    @ColumnInfo(name = "TIME")
    private String time;

    // Getters and setters are ignored for brevity,
    // but they're required for Room to work.

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

