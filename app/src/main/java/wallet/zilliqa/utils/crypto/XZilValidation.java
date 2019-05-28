package wallet.zilliqa.utils.crypto;

import com.firestack.laksaj.utils.ByteUtil;
import com.firestack.laksaj.utils.HashUtil;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XZilValidation {
  //using low case
  public static boolean isByteString(String str, int len) {
    Pattern pattern = Pattern.compile("^(0x)?[0-9a-fA-F]{" + len + "}");
    Matcher matcher = pattern.matcher(str);
    return matcher.matches();
  }

  public static boolean isBech32(String str) {
    Pattern pattern = Pattern.compile("^zil1[qpzry9x8gf2tvdw0s3jn54khce6mua7l]{38}");
    Matcher matcher = pattern.matcher(str);
    return matcher.matches();
  }

  public static boolean isAddress(String address) {
    return isByteString(address, 40);
  }

  public static boolean isPublicKey(String publicKey) {
    return isByteString(publicKey, 66);
  }

  public static boolean isPrivateKey(String privateKey) {
    return isByteString(privateKey, 64);
  }

  public static boolean isSignature(String signature) {
    return isByteString(signature, 128);
  }

  public static boolean isValidChecksumAddress(String address) {
    return isAddress(address.replace("0x", "")) && toCheckSumAddress(address)
        .equals(address);
  }

  public static String intToHex(int value, int size) {
    String hexVal = Integer.toHexString(value);
    char[] hexRep = new char[hexVal.length()];
    for (int i = 0; i < hexVal.length(); i++) {
      hexRep[i] = hexVal.charAt(i);
    }

    List<Character> hex = new ArrayList<>();

    for (int i = 0; i < size - hexVal.length(); i++) {
      hex.add('0');
    }

    for (int i = 0; i < hexVal.length(); i++) {
      hex.add(hexRep[i]);
    }

    StringBuilder builder = new StringBuilder();
    for (Character c : hex) {
      builder.append(c);
    }
    return builder.toString();
  }

  public static String toCheckSumAddress(String address)  {
    address = address.toLowerCase().replace("0x", "");
    String hash = ByteUtil.byteArrayToHexString(HashUtil.sha256(ByteUtil.hexStringToByteArray(address)));
    StringBuilder ret = new StringBuilder("0x");
    BigInteger v = new BigInteger(ByteUtil.hexStringToByteArray(hash));
    for (int i = 0; i < address.length(); i++) {
      if ("1234567890".indexOf(address.charAt(i)) != -1) {
        ret.append(address.charAt(i));
      } else {
        BigInteger checker = v.and(BigInteger.valueOf(2l).pow(255 - 6 * i));
        ret.append(checker.compareTo(BigInteger.valueOf(1l)) < 0 ? String.valueOf(address.charAt(i)).toLowerCase() : String.valueOf(address.charAt(i)).toUpperCase());
      }
    }
    return ret.toString();
  }

}
