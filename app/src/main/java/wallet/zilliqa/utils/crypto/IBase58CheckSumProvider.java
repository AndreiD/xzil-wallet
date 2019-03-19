package wallet.zilliqa.utils.crypto;

/**
 * @author fanyongpeng
 * @create 2018-08-13 11:55
 **/
public interface IBase58CheckSumProvider {
    byte[] calculateActualCheckSum(byte[] var1);
}
