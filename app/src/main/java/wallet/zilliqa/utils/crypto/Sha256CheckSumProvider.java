package wallet.zilliqa.utils.crypto;

public class Sha256CheckSumProvider implements IBase58CheckSumProvider {
    public Sha256CheckSumProvider() {
    }

    public byte[] calculateActualCheckSum(byte[] data) {
        return Sha256Hash.hashTwice(data);
    }
}