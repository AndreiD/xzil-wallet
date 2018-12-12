package wallet.zilliqa;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Constants {
  private Constants() {
  }

  // I know you can see them, please don't drain them of fake money :)
  public static String newWalletPublicAddress = "573EC96638C8BB1C386394602E1460634F02ADDD";
  public static String newWalletPrivateKey = "69122B6C3A70B6CC7908546B7F6233F1F5501ECC5759D2940E32FEE250E7AA7A";

  public static String newWalletPublicAddress2 = "37182D3C7033D76080E04F70BED76A0496D14CD1";
  public static String newWalletPrivateKey2 = "BD04FFE30016A975B4193468A54A852F58F10E913BE8945C1C33CB5EA0CF5881";

  public static String publicAddress = "";
  public static String lastScanAddress = "";
  public static final String TESTADDRESS = "0x000000591672c2Ad77D99f62BE38Eb2C995bb09c"; // used to autofill when app is in debug mode

  public static String getBlockchanPreference(Context ctx) {

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    String networkPreference = prefs.getString("network_preference", "");

    switch (networkPreference) {
      case "mainnet":
        return "https://mainnet.infura.io/";
      case "testnet":
        return "https://rinkeby.infura.io/";
      default:
        return "https://rinkeby.infura.io/\"";
    }
  }

  public static String getExplorerUrl(Context ctx) {

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    String networkPreference = prefs.getString("network_preference", "");

    switch (networkPreference) {
      case "mainnet":
        return "https://explorer-scilla.zilliqa.com/transactions/";
      case "testnet":
        return "https://explorer-scilla.zilliqa.com/transactions/";
      default:
        return "https://explorer-scilla.zilliqa.com/transactions/";
    }
  }

  public static DecimalFormat getDecimalFormat() {
    DecimalFormat df = new DecimalFormat("#.########");
    df.setRoundingMode(RoundingMode.FLOOR);
    return df;
  }
}
