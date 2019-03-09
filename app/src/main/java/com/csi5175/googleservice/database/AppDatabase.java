/*
 * Yan Zhang
 * 300052103
 * initiate database for Room DAO
 * */
package com.csi5175.googleservice.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

/*
 * The room ORM is really helpful when interact with sqlite
 * following tutorials introduced the basic operations need to use Room
 * https://www.jianshu.com/p/7354d5048597
 * */
@Database(entities = {UserActivityState.class}, version = 1)
@TypeConverters({ConverterFactory.class})
public abstract class AppDatabase extends RoomDatabase {
    //abstract a dao from interface
    public abstract DAO activityDao();

    //singleton database for access
    private static AppDatabase INSTANCE;
    private static final Object sLock = new Object();

    public static AppDatabase getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "userActivity.db")
                        .allowMainThreadQueries()
                        .build();
            }
            return INSTANCE;
        }
    }
}
