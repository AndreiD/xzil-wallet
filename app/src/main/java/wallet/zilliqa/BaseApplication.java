package wallet.zilliqa;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.socks.library.KLog;
import io.reactivex.plugins.RxJavaPlugins;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.PreferencesHelper;

public class BaseApplication extends Application {

  private static PreferencesHelper preferencesHelper;
  private static AppDatabase roomDb;

  @Override public void onCreate() {
    super.onCreate();

    boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    if (isDebuggable) {
      KLog.init(true);
    } else {
      KLog.init(false);
    }


    //fonts init
    CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/regular.otf")
        .setFontAttrId(R.attr.fontPath)
        .build()
    );

    RxJavaPlugins.setErrorHandler(KLog::e);
  }

  @Override public void onLowMemory() {
    super.onLowMemory();
    KLog.w("onLowMemory triggered");
  }


  /**
   * Gets the PreferencesHelper
   */
  public static PreferencesHelper getPreferencesHelper(Context ctx) {
    if (preferencesHelper == null) {
      preferencesHelper = new PreferencesHelper(ctx);
    }
    return preferencesHelper;
  }

  public static AppDatabase getAppDatabase(Context ctx) {
    if (roomDb == null) {
      roomDb = AppDatabase.getAppDatabase(ctx);
      return roomDb;
    }
    return roomDb;
  }

  public static BaseApplication get(Context context) {
    return (BaseApplication) context.getApplicationContext();
  }
}
