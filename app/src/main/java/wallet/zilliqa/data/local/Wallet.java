package wallet.zilliqa.data.local;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "wallets_table", indices = { @Index(value = "address", unique = true) })
public class Wallet {

  @PrimaryKey(autoGenerate = true)
  private int uid;

  @ColumnInfo(name = "address")
  private String address;

  @ColumnInfo(name = "encrypted_private_key")
  private String encrypted_private_key;


  public Wallet(String address, String encrypted_private_key) {
    this.address = address;
    this.encrypted_private_key = encrypted_private_key;
  }

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getEncrypted_private_key() {
    return encrypted_private_key;
  }

  public void setEncrypted_private_key(String encrypted_private_key) {
    this.encrypted_private_key = encrypted_private_key;
  }

  @Override public String toString() {
    return "Wallet{" +
        "uid=" + uid +
        ", address='" + address + '\'' +
        ", encrypted_private_key='" + encrypted_private_key + '\'' +
        '}';
  }
}