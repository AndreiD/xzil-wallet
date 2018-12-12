package wallet.zilliqa.data.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import io.reactivex.Maybe;
import java.util.List;

@Dao
public interface WalletDAO {

  @Query("SELECT * FROM wallets_table")
  Maybe<List<Wallet>> getAll();

  @Query("SELECT * FROM wallets_table where address LIKE :address")
  Maybe<Wallet> findByAddress(String address);

  @Query("SELECT COUNT(*) from wallets_table")
  Maybe<Long> totalWallets();

  @Insert
  List<Long> insertAll(Wallet... wallets);

  @Delete
  void delete(Wallet wallet);

}