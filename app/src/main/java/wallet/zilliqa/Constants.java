package wallet.zilliqa;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Constants {
  private Constants() {
  }

  // I know you can see them, please don't drain them of fake money :)
  public static String newWalletPublicAddress = "0xDACD3891D0CF9446BB5EBED65AA8665F98018B04";
  public static String newWalletPrivateKey = "5a43854ae5c2e689e04cecd8a1672bfcf86d62349ab4cd39c2db8fe73e824518";

  // 2nd wallet
  public static String secondWalletPrivateKey = "2a6f409db55e6b8c33a833bd747f3b523164300d32960c6af41bfa747ecbb042";
  public static String secondWalletAddress = "9F4E1F751E48F5A9078C8E59090BB8D7B61CA8D0";

  public static String lastScanAddress = "";

 // public static final String EXPLORER_URL = "https://dev-explorer.zilliqa.com/transactions/";
 public static final String EXPLORER_URL = "https://viewblock.io/zilliqa/tx/";


  public static DecimalFormat getDecimalFormat() {
    DecimalFormat df = new DecimalFormat("#.########");
    df.setRoundingMode(RoundingMode.FLOOR);
    return df;
  }
}
