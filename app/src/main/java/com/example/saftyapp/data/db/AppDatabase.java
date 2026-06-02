package com.example.saftyapp.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.saftyapp.data.db.dao.ContactDao;
import com.example.saftyapp.data.db.dao.EmergencySessionDao;
import com.example.saftyapp.data.db.dao.LocationLogDao;
import com.example.saftyapp.data.db.dao.SmsLogDao;
import com.example.saftyapp.data.db.entity.Contact;
import com.example.saftyapp.data.db.entity.EmergencySession;
import com.example.saftyapp.data.db.entity.LocationLog;
import com.example.saftyapp.data.db.entity.SmsLog;

@Database(entities = {Contact.class, EmergencySession.class, LocationLog.class, SmsLog.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ContactDao contactDao();
    public abstract EmergencySessionDao emergencySessionDao();
    public abstract LocationLogDao locationLogDao();
    public abstract SmsLogDao smsLogDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "safety_app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
