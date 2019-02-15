package wallet.zilliqa.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import wallet.zilliqa.data.local.PreferencesHelper;

//TODO: needs refactoring...
public class Cryptography {

  private static final String ANDROID_KEY_STORE_NAME = "AndroidKeyStore";
  private static final String AES_MODE_M_OR_GREATER = "AES/GCM/NoPadding";
  private static final String KEY_ALIAS = "MyApp-KeyAliasForEncryption";
  private static final byte[] FIXED_IV = new byte[] {
      51, 52, 53, 52, 51, 51,
      49, 48, 47,
      46, 45, 42
  };
  private static final String CHARSET_NAME = "UTF-8";
  private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
  private static final String CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA = "AndroidOpenSSL";
  private static final String SHARED_PREFERENCE_NAME = "YOUR-EncryptedKeysSharedPreferences";
  private static final String ENCRYPTED_KEY_NAME = "YOUR-EncryptedKeysKeyName";
  private static final String TAG = Cryptography.class.getName();

  private final Context mContext;

  private final static Object s_keyInitLock = new Object();

  public Cryptography(Context context) {
    mContext = context;
  }

  // Using algorithm as described at https://medium.com/@ericfu/securely-storing-secrets-in-an-android-application-501f030ae5a3
  private void initKeys()
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException,
      NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableEntryException {
    KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
    keyStore.load(null);

    if (!keyStore.containsAlias(KEY_ALIAS)) {
      initValidKeys();
    } else {
      boolean keyValid = false;
      try {
        KeyStore.Entry keyEntry = keyStore.getEntry(KEY_ALIAS, null);
        if (keyEntry instanceof KeyStore.SecretKeyEntry &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          keyValid = true;
        }
      } catch (NullPointerException | UnrecoverableKeyException e) {
        Log.e(TAG, "Failed to get key store entry", e);
      }

      if (!keyValid) {
        synchronized (s_keyInitLock) {
          // System upgrade or something made key invalid
          removeKeys(keyStore);
          initValidKeys();
        }
      }
    }
  }

  //
  //public static String toCheckSumAddress(String address) {
  //  address = address.toLowerCase().replace("0x", "");
  //  String hash = ByteUtil.byteArrayToHexString(HashUtil.sha256(ByteUtil.hexStringToByteArray(address)));
  //  StringBuilder ret = new StringBuilder("0x");
  //  BigInteger v = new BigInteger(ByteUtil.hexStringToByteArray(hash));
  //  for (int i = 0; i < address.length(); i++) {
  //    if ("1234567890".indexOf(address.charAt(i)) != -1) {
  //      ret.append(address.charAt(i));
  //    } else {
  //      BigInteger checker = v.and(BigInteger.valueOf(2l).pow(255 - 6 * i));
  //      ret.append(checker.compareTo(BigInteger.valueOf(1l)) < 0 ? String.valueOf(address.charAt(i)).toLowerCase() : String.valueOf(address.charAt(i)).toUpperCase());
  //    }
  //  }
  //  return ret.toString();
  //}
  //
  private void removeKeys(KeyStore keyStore) throws KeyStoreException {
    keyStore.deleteEntry(KEY_ALIAS);
    removeSavedSharedPreferences();
  }

  private void initValidKeys()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {

    synchronized (s_keyInitLock) {
      generateKeysForAPIMOrGreater();
    }
  }

  @SuppressLint("ApplySharedPref")
  private void removeSavedSharedPreferences() {
    PreferencesHelper pref = new PreferencesHelper(mContext);
    pref.clear();
    Log.d(TAG, "Cleared secret key shared preferences");
  }

  @SuppressLint("ApplySharedPref")
  private void saveEncryptedKey()
      throws CertificateException, NoSuchPaddingException, InvalidKeyException,
      NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException,
      UnrecoverableEntryException, IOException {
    SharedPreferences pref =
        mContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    String encryptedKeyBase64encoded = pref.getString(ENCRYPTED_KEY_NAME, null);
    if (encryptedKeyBase64encoded == null) {
      byte[] key = new byte[16];
      SecureRandom secureRandom = new SecureRandom();
      secureRandom.nextBytes(key);
      byte[] encryptedKey = rsaEncryptKey(key);
      encryptedKeyBase64encoded = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
      SharedPreferences.Editor edit = pref.edit();
      edit.putString(ENCRYPTED_KEY_NAME, encryptedKeyBase64encoded);
      boolean successfullyWroteKey = edit.commit();
      if (successfullyWroteKey) {
        Log.d(TAG, "Saved keys successfully");
      } else {
        Log.e(TAG, "Saved keys unsuccessfully");
        throw new IOException("Could not save keys");
      }
    }
  }


  private void generateKeysForAPIMOrGreater()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
    KeyGenerator keyGenerator;
    keyGenerator =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE_NAME);
    keyGenerator.init(
        new KeyGenParameterSpec.Builder(KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            // NOTE no Random IV. According to above this is less secure but acceptably so.
            .setRandomizedEncryptionRequired(false)
            .build());
    // Note according to [docs](https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.html)
    // this generation will also add it to the keystore.
    keyGenerator.generateKey();
  }

  public String encryptData(String stringDataToEncrypt)
      throws NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableEntryException,
      CertificateException, KeyStoreException, IOException, InvalidAlgorithmParameterException,
      InvalidKeyException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException {

    initKeys();

    if (stringDataToEncrypt == null) {
      throw new IllegalArgumentException("Data to be decrypted must be non null");
    }

    Cipher cipher;

      cipher = Cipher.getInstance(AES_MODE_M_OR_GREATER);
      cipher.init(Cipher.ENCRYPT_MODE, getSecretKeyAPIMorGreater(),
          new GCMParameterSpec(128, FIXED_IV));


    byte[] encodedBytes = cipher.doFinal(stringDataToEncrypt.getBytes(CHARSET_NAME));
    return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
  }

  public String decryptData(String encryptedData)
      throws NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableEntryException,
      CertificateException, KeyStoreException, IOException, InvalidAlgorithmParameterException,
      InvalidKeyException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException {

    initKeys();

    if (encryptedData == null) {
      throw new IllegalArgumentException("Data to be decrypted must be non null");
    }

    byte[] encryptedDecodedData = Base64.decode(encryptedData, Base64.DEFAULT);

    Cipher c;
    try {

        c = Cipher.getInstance(AES_MODE_M_OR_GREATER);
        c.init(Cipher.DECRYPT_MODE, getSecretKeyAPIMorGreater(),
            new GCMParameterSpec(128, FIXED_IV));

    } catch (InvalidKeyException | IOException e) {
      // Since the keys can become bad (perhaps because of lock screen change)
      // drop keys in this case.
      removeKeys();
      throw e;
    }

    byte[] decodedBytes = c.doFinal(encryptedDecodedData);
    return new String(decodedBytes, CHARSET_NAME);
  }

  private Key getSecretKeyAPIMorGreater()
      throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException,
      UnrecoverableKeyException {
    KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
    keyStore.load(null);
    return keyStore.getKey(KEY_ALIAS, null);
  }

  private byte[] rsaEncryptKey(byte[] secret)
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException,
      NoSuchProviderException, NoSuchPaddingException, UnrecoverableEntryException,
      InvalidKeyException {

    KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
    keyStore.load(null);

    KeyStore.PrivateKeyEntry privateKeyEntry =
        (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
    Cipher inputCipher =
        Cipher.getInstance(RSA_MODE, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA);
    inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
    cipherOutputStream.write(secret);
    cipherOutputStream.close();

    return outputStream.toByteArray();
  }

  private byte[] rsaDecryptKey(byte[] encrypted)
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException,
      UnrecoverableEntryException, NoSuchProviderException, NoSuchPaddingException,
      InvalidKeyException {

    KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
    keyStore.load(null);

    KeyStore.PrivateKeyEntry privateKeyEntry =
        (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
    Cipher output = Cipher.getInstance(RSA_MODE, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA);
    output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
    CipherInputStream cipherInputStream = new CipherInputStream(
        new ByteArrayInputStream(encrypted), output);
    ArrayList<Byte> values = new ArrayList<>();
    int nextByte;
    while ((nextByte = cipherInputStream.read()) != -1) {
      values.add((byte) nextByte);
    }

    byte[] decryptedKeyAsBytes = new byte[values.size()];
    for (int i = 0; i < decryptedKeyAsBytes.length; i++) {
      decryptedKeyAsBytes[i] = values.get(i);
    }
    return decryptedKeyAsBytes;
  }

  public void removeKeys()
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
    synchronized (s_keyInitLock) {
      KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
      keyStore.load(null);
      removeKeys(keyStore);
    }
  }
}