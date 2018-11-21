package wallet.zilliqa;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import com.socks.library.KLog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class BaseApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();

    boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    if (isDebuggable) {
      KLog.init(true);
    } else {
      KLog.init(false);
    }



    CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/regular.ttf")
        .setFontAttrId(R.attr.fontPath)
        .build()
    );
  }

  @Override public void onLowMemory() {
    super.onLowMemory();
    KLog.w("onLowMemory triggered");
  }
}
