package wallet.zilliqa.data.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Contact.class, Wallet.class}, version = 7)
public abstract class AppDatabase extends RoomDatabase {

  public static final String DBNAME = "app-database";
  private static AppDatabase INSTANCE;
  public abstract ContactDAO contactDao();
  public abstract WalletDAO walletDao();

  public static AppDatabase getAppDatabase(Context context) {
    if (INSTANCE == null) {
      INSTANCE =
          Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DBNAME)
              // allow queries on the main thread.
              // Don't do this on a real app! See PersistenceBasicSample for an example.
              .fallbackToDestructiveMigration()
              .allowMainThreadQueries()
              .build();
    }
    return INSTANCE;
  }

  public static void destroyInstance() {
    INSTANCE = null;
  }
}