package com.trailblazing.scanmage.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.trailblazing.scanmage.model.ScannedFile;

import java.util.List;

@Dao
public interface FilesDao {
    @Query("SELECT * FROM files")
    List<ScannedFile> getAll();

    @Insert
    long insert(ScannedFile geoVideo);

    @Update
    void update(ScannedFile geoVideo);

    @Delete
    void delete(ScannedFile geoVideo);

}
