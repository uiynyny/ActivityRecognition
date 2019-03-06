package com.csi5175.googleservice.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class ConverterFactory {
    @TypeConverter
    public static long fromDatetoLong(Date date) {
        return date == null ? null : date.getTime();
    }

    public static Date fromLongtoDate(Long value) {
        return value == null ? null : new Date(value);
    }
}
