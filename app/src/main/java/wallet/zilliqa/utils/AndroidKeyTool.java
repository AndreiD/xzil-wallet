package wallet.zilliqa.utils;

import com.firestack.laksaj.crypto.KDFType;
import com.firestack.laksaj.crypto.KeyStore;
import com.firestack.laksaj.utils.ByteUtil;
import com.firestack.laksaj.utils.HashUtil;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.jce.spec.ECNamedCurveGenParameterSpec;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;

//import org.bouncycastle.asn1.x9.X9ECParameters;
//import org.bouncycastle.crypto.ec.CustomNamedCurves;
//import org.bouncycastle.crypto.params.ECDomainParameters;
//import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
//import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.jce.spec.ECNamedCurveGenParameterSpec;
//import org.bouncycastle.math.ec.ECPoint;
//import org.bouncycastle.math.ec.FixedPointCombMultiplier;


import org.web3j.crypto.ECKeyPair;
import java.security.*;

public class AndroidKeyTool {
  public static final ECDomainParameters CURVE;
  private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
  private static final KeyStore keystore = KeyStore.defaultKeyStore();
  private static final Pattern pattern = Pattern.compile("^(0x)?[0-9a-f]");

  public AndroidKeyTool() {
  }



  public static ECKeyPair generateKeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC","BC");
    keyPairGenerator.initialize(new ECGenParameterSpec("secp256k1"));
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    //BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
    //BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();

    PrivateKey privateKey = keyPair.getPrivate();
    PublicKey publicKey = keyPair.getPublic();

    BigInteger bigPrivKey = new BigInteger(privateKey.getEncoded());
    BigInteger bigPubKey = new BigInteger(publicKey.getEncoded());

    return new ECKeyPair(bigPrivKey, bigPubKey);
  }



  public static String getAddressFromPrivateKey(String privateKey) {
    String publicKey = getPublicKeyFromPrivateKey(privateKey, true);
    return getAddressFromPublicKey(publicKey);
  }

  public static boolean isByteString(String address) {
    System.out.println(address);
    Matcher matcher = pattern.matcher(address);

    while(matcher.find()) {
      System.out.println(matcher.group());
      System.out.print("start:" + matcher.start());
      System.out.println(" end:" + matcher.end());
    }

    return true;
  }

  public static String getPublicKeyFromPrivateKey(String privateKey, boolean compressed) {
    byte[] pk = ByteUtil.hexStringToByteArray(privateKey);
    BigInteger bigInteger = new BigInteger(1, pk);
    ECPoint point = getPublicPointFromPrivate(bigInteger);
    return ByteUtil.byteArrayToHexString(point.getEncoded(compressed));
  }

  public static String getAddressFromPublicKey(String publicKey) {
    byte[] data = getAddressFromPublicKey(ByteUtil.hexStringToByteArray(publicKey));
    String originAddress = ByteUtil.byteArrayToHexString(data);
    return originAddress.substring(24);
  }

  public static byte[] getAddressFromPublicKey(byte[] publicKey) {
    return HashUtil.sha256(publicKey);
  }

  public static byte[] generateRandomBytes(int size) {
    byte[] bytes = new byte[size];
    (new SecureRandom()).nextBytes(bytes);
    return bytes;
  }

  private static ECPoint getPublicPointFromPrivate(BigInteger privateKeyPoint) {
    if (privateKeyPoint.bitLength() > CURVE.getN().bitLength()) {
      privateKeyPoint = privateKeyPoint.mod(CURVE.getN());
    }

    return (new FixedPointCombMultiplier()).multiply(CURVE.getG(), privateKeyPoint);
  }

  public static String decryptPrivateKey(String file, String passphrase) throws Exception {
    return keystore.decryptPrivateKey(file, passphrase);
  }

  public static String encryptPrivateKey(String privateKey, String passphrase, KDFType type) throws Exception {
    return keystore.encryptPrivateKey(privateKey, passphrase, type);
  }

  static {
    CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
  }
}
