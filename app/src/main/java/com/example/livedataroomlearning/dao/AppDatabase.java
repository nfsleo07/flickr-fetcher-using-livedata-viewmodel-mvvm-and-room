package com.example.livedataroomlearning.dao;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Manpreet Anand on 5/4/19.
 */
@Database(entities = {Photo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PhotoDao photoDao();
}
