package wallet.zilliqa.utils.crypto;

import org.spongycastle.crypto.digests.RIPEMD160Digest;

public class RIPEMD160CheckSumProvider implements IBase58CheckSumProvider {
    public RIPEMD160CheckSumProvider() {
    }

    public byte[] calculateActualCheckSum(byte[] data) {
        RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
        ripemd160Digest.update(data, 0, data.length);
        byte[] actualChecksum = new byte[ripemd160Digest.getDigestSize()];
        ripemd160Digest.doFinal(actualChecksum, 0);
        return actualChecksum;
    }
}
