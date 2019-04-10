package wallet.zilliqa.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.inputmethod.InputMethodManager;
import com.socks.library.KLog;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;

//TODO: delete not used
public class DUtils {

  static final String HEXES = "0123456789ABCDEF";

  public static String getDeviceInfo() {
    String manufacturer = Build.MANUFACTURER;

    String model =
        Build.MODEL + " " + android.os.Build.BRAND + " (" + android.os.Build.VERSION.RELEASE + ")" + " API-" + android.os.Build.VERSION.SDK_INT;

    if (model.startsWith(manufacturer)) {
      return capitalize(model);
    } else {
      return capitalize(manufacturer) + " " + model;
    }
  }

  public static String getUniqueID() {
    return getSHA256(getDeviceInfo());
  }

  public static String loadFileFromAssets(Context context, String filename) {
    try {
      InputStream is = context.getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public static Fragment getVisibleFragment(AppCompatActivity activity) {
    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    List<Fragment> fragments = fragmentManager.getFragments();
    for (Fragment fragment : fragments) {
      if (fragment != null && fragment.isVisible()) {
        return fragment;
      }
    }
    return null;
  }

  public static String getShortID() {
    return getUniqueID().substring(0, 5);
  }

  private static String capitalize(String s) {
    if (s == null || s.length() == 0) {
      return s;
    } else {
      StringBuilder phrase = new StringBuilder();
      boolean next = true;
      for (char c : s.toCharArray()) {
        if (next && Character.isLetter(c) || Character.isWhitespace(c)) next = Character.isWhitespace(c = Character.toUpperCase(c));
        phrase.append(c);
      }
      return phrase.toString();
    }
  }

  public static void hideKeyboard(Context ctx) {
    InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
    if (imm == null) {
      return;
    }
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
  }

  @SuppressLint("NewApi") private static String getSHA256(String plainText) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(plainText.getBytes(Charset.forName("UTF-8")));
      return getHex(md.digest()).toLowerCase();
    } catch (Exception ex) {
      KLog.e(ex);
    }
    return null;
  }

  private static String getHex(byte[] raw) {
    if (raw == null) {
      return null;
    }
    final StringBuilder hex = new StringBuilder(2 * raw.length);
    for (final byte b : raw) {
      hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
    }
    return hex.toString();
  }

  public static String randomString(int len) {
    final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    SecureRandom rnd = new SecureRandom();
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++)
      sb.append(AB.charAt(rnd.nextInt(AB.length())));
    return sb.toString();
  }

  /**
   * Colorize a specific substring in a string for TextView. Use it like this: <pre>
   * textView.setText(
   *     Strings.colorized("The some words are black some are the default.","black", Color.BLACK),
   *     TextView.BufferType.SPANNABLE
   * );
   * </pre>
   *
   * @param text Text that contains a substring to colorize
   * @param word The substring to colorize
   * @param argb The color
   * @return the Spannable for TextView's consumption
   */
  public static Spannable colorized(final String text, final String word, final int argb) {
    final Spannable spannable = new SpannableString(text);
    int substringStart = 0;
    int start;
    while ((start = text.indexOf(word, substringStart)) >= 0) {
      spannable.setSpan(
          new ForegroundColorSpan(argb), start, start + word.length(),
          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
      );
      substringStart = start + word.length();
    }
    return spannable;
  }
}
