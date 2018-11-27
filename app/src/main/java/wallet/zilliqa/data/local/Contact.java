package wallet.zilliqa.data.local;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "contacts_table", indices = {@Index(value = "address", unique = true)})
public class Contact {

  @PrimaryKey(autoGenerate = true)
  private int uid;

  @ColumnInfo(name = "name")
  private String name;

  @ColumnInfo(name = "address")
  private String address;

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}