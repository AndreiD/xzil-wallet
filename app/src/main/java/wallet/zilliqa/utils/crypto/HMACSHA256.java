package wallet.zilliqa.utils.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * author: fanyongpeng
 * date: 2019/2/19
 **/
public class HMACSHA256 {
    /**
     * sha256_HMAC
     * @param message message
     * @param secret  secret
     * @return
     */
    public static byte[] sha256_HMAC(byte[] message, byte[] secret) {
        byte[] hash;
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret, "HmacSHA256");
            sha256_HMAC.init(secret_key);
            hash = sha256_HMAC.doFinal(message);
        } catch (Exception e) {
            throw new RuntimeException("Error HmacSHA256 ===========" + e.getMessage());
        }
        return hash;
    }
}
