package com.trailblazing.scanmage.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.trailblazing.scanmage.model.ScannedFile;

@Database(entities = {ScannedFile.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance = null;

    public abstract FilesDao filesDao();

    public static AppDatabase getInstance(Context context) {
        if (instance != null) {
            return instance;
        }
        synchronized (context.getApplicationContext()) {
            instance = Room
                    .databaseBuilder(context.getApplicationContext(), AppDatabase.class, "scanmage.db")
                    .allowMainThreadQueries()
                    .build();
            return instance;
        }
    }
}
