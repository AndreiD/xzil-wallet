package wallet.zilliqa.utils.crypto;


/**
 * @author fanyongpeng
 * @create 2018-08-13 11:40
 **/
public class KeyPair implements java.io.Serializable {
    private String priKey;
    private String pubKey;

    public KeyPair() {
    }

    public boolean valid() {
        if (this.priKey == null || "".equals(this.priKey)) {
            return false;
        } else {
            return !(this.pubKey == null || "".equals(this.pubKey));
        }
    }

    public KeyPair(String privateKey, String publicKey) {
        this.priKey = privateKey;
        this.pubKey = publicKey;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof KeyPair)) {
            return false;
        } else {
            KeyPair keyPair = (KeyPair)o;
            if (this.priKey != null) {
                if (this.priKey.equals(keyPair.priKey)) {
                    return this.pubKey != null ? this.pubKey.equals(keyPair.pubKey) : keyPair.pubKey == null;
                }
            } else if (keyPair.priKey == null) {
                return this.pubKey != null ? this.pubKey.equals(keyPair.pubKey) : keyPair.pubKey == null;
            }

            return false;
        }
    }

    public int hashCode() {
        int result = this.priKey != null ? this.priKey.hashCode() : 0;
        result = 31 * result + (this.pubKey != null ? this.pubKey.hashCode() : 0);
        return result;
    }

    public String getAddress() {
        return ECKey.pubKey2Base58Address(this.pubKey);
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPriKey() {
        return this.priKey;
    }

    public String getPubKey() {
        return this.pubKey;
    }
}
