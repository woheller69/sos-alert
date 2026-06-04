package org.sosalerter.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import org.sosalerter.app.data.db.entity.Contact;

import java.util.List;

@Dao
public interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Contact contact);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact contact);

    @Query("SELECT * FROM contacts ORDER BY priority ASC")
    LiveData<List<Contact>> getAllContacts();

    @Query("SELECT * FROM contacts ORDER BY priority ASC")
    List<Contact> getAllContactsSync();

    @Query("SELECT COUNT(*) FROM contacts")
    int getContactCount();
}
