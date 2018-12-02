package wallet.zilliqa.data.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import java.util.List;

@Dao
public interface ContactDAO {

  @Query("SELECT * FROM contacts_table")
  List<Contact> getAll();

  @Query("SELECT * FROM contacts_table where name LIKE :name")
  Contact findByName(String name);

  @Query("SELECT COUNT(*) from contacts_table")
  int totalContacts();

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(Contact... contacts);

  @Delete
  void delete(Contact contact);

  @Query("DELETE FROM contacts_table") void nukeContacts();
}