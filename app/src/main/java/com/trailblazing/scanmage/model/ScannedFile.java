package com.trailblazing.scanmage.model;

import androidx.room.Entity;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

@Entity(tableName = "files")
public class ScannedFile {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name="file_path")
    public String filePath;

    @ColumnInfo(name = "date")
    public String date;
}
