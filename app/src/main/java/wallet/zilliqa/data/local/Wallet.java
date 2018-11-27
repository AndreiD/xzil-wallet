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

  @ColumnInfo(name = "seed")
  private String seed;

  @ColumnInfo(name = "password")
  private String password;

  public Wallet(String address, String seed, String password) {
    this.address = address;
    this.seed = seed;
    this.password = password;
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

  public String getSeed() {
    return seed;
  }

  public void setSeed(String seed) {
    this.seed = seed;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override public String toString() {
    return "Wallet{" +
        "uid=" + uid +
        ", address='" + address + '\'' +
        ", seed='" + seed + '\'' +
        ", password='" + password + '\'' +
        '}';
  }
}