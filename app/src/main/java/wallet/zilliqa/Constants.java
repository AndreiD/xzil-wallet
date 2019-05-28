package wallet.zilliqa;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.socks.library.KLog;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import org.checkerframework.checker.units.qual.K;

import static com.firestack.laksaj.account.Wallet.pack;

public class Constants {
  private Constants() {
  }

  // I know you can see them, please don't drain them of fake money :)
  public static String newWalletPublicAddress = "0xDACD3891D0CF9446BB5EBED65AA8665F98018B04";
  public static final String newWalletPrivateKey = "5a43854ae5c2e689e04cecd8a1672bfcf86d62349ab4cd39c2db8fe73e824518";

  // 2nd wallet
  public static String secondWalletPrivateKey = "2a6f409db55e6b8c33a833bd747f3b523164300d32960c6af41bfa747ecbb042";
  public static final String secondWalletAddress = "zil1xdedlquml4j6q8gsef5vmvqum8dw0yekdtg6vu";

  public static String lastScanAddress = "";

  public static final String EXPLORER_URL_TESTNET = "https://dev-explorer.zilliqa.com/transactions/";
  public static final String EXPLORER_URL_MAINNET = "https://viewblock.io/zilliqa/tx/";

  public static final String API_URL_TESTNET = "https://dev-api.zilliqa.com";
  public static final String API_URL_MAINNET = "https://api.zilliqa.com";

  public static boolean isMaiNet(Context ctx) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    if (prefs.getString("network_preference", "mainnet").equals("mainnet")) {
      KLog.d("MainNet is selected...................");
      return true;
    } else {
      KLog.d("TestNet is selected...................");
      return false;
    }
  }

  /**
   * Gets the Network API URL
   */
  public static String getNetworkAPIURL(Context ctx) {
    if (Constants.isMaiNet(ctx)) {
      return API_URL_MAINNET;
    } else {
      return API_URL_TESTNET;
    }
  }

  /**
   * Gets the Explorer URL
   */
  public static String getExplorerURL(Context ctx) {
    if (Constants.isMaiNet(ctx)) {
      return EXPLORER_URL_MAINNET;
    } else {
      return EXPLORER_URL_TESTNET;
    }
  }

  /**
   * Gets the Version URL
   */
  public static String getVersion(Context ctx) {
    if (Constants.isMaiNet(ctx)) {
      return String.valueOf(pack(1, 8));
    } else {
      return String.valueOf(pack(333, 8));
    }
  }
}
