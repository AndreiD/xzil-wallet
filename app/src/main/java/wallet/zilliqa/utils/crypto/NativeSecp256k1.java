package wallet.zilliqa.utils.crypto;

import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NativeSecp256k1 {
    private static final int TOTAL_CAPACITY = 520;
    private static final int CAPACITY = 32;
    private static final ReentrantReadWriteLock RWL = new ReentrantReadWriteLock();
    private static final Lock R_LOCK;
    private static final ThreadLocal<ByteBuffer> nativeECDSABuffer;

    public NativeSecp256k1() {
    }

    public static boolean verify(byte[] data, byte[] signature, byte[] pub) throws Exception {
        Preconditions.checkArgument(data.length == 32 && signature.length <= 520 && pub.length <= 520);
        ByteBuffer byteBuff = (ByteBuffer)nativeECDSABuffer.get();
        if (byteBuff == null || byteBuff.capacity() < 520) {
            byteBuff = ByteBuffer.allocateDirect(520);
            byteBuff.order(ByteOrder.nativeOrder());
            nativeECDSABuffer.set(byteBuff);
        }

        byteBuff.rewind();
        byteBuff.put(data);
        byteBuff.put(signature);
        byteBuff.put(pub);
        R_LOCK.lock();

        boolean var4;
        try {
            var4 = secp256k1_ecdsa_verify(byteBuff, Secp256k1Context.getContext(), signature.length, pub.length) == 1;
        } finally {
            R_LOCK.unlock();
            nativeECDSABuffer.remove();
        }

        return var4;
    }

    public static byte[] sign(byte[] data, byte[] sec) throws Exception {
        Preconditions.checkArgument(data.length == 32 && sec.length <= 32);
        ByteBuffer byteBuff = (ByteBuffer)nativeECDSABuffer.get();
        if (byteBuff == null || byteBuff.capacity() < 64) {
            byteBuff = ByteBuffer.allocateDirect(64);
            byteBuff.order(ByteOrder.nativeOrder());
            nativeECDSABuffer.set(byteBuff);
        }

        byteBuff.rewind();
        byteBuff.put(data);
        byteBuff.put(sec);
        R_LOCK.lock();

        byte[][] retByteArray;
        try {
            retByteArray = secp256k1_ecdsa_sign(byteBuff, Secp256k1Context.getContext());
        } finally {
            R_LOCK.unlock();
        }

        byte[] sigArr = retByteArray[0];
        int sigLen = (new BigInteger(new byte[]{retByteArray[1][0]})).intValue();
        int retVal = (new BigInteger(new byte[]{retByteArray[1][1]})).intValue();
        NativeSecp256k1Util.assertEquals(sigArr.length, sigLen, "Got bad signature length.");
        return retVal == 0 ? new byte[0] : sigArr;
    }

    private static native long secp256k1_ctx_clone(long var0);

    private static native int secp256k1_context_randomize(ByteBuffer var0, long var1);

    private static native byte[][] secp256k1_privkey_tweak_add(ByteBuffer var0, long var1);

    private static native byte[][] secp256k1_privkey_tweak_mul(ByteBuffer var0, long var1);

    private static native byte[][] secp256k1_pubkey_tweak_add(ByteBuffer var0, long var1, int var3);

    private static native byte[][] secp256k1_pubkey_tweak_mul(ByteBuffer var0, long var1, int var3);

    private static native void secp256k1_destroy_context(long var0);

    private static native int secp256k1_ecdsa_verify(ByteBuffer var0, long var1, int var3, int var4);

    private static native byte[][] secp256k1_ecdsa_sign(ByteBuffer var0, long var1);

    private static native int secp256k1_ec_seckey_verify(ByteBuffer var0, long var1);

    private static native byte[][] secp256k1_ec_pubkey_create(ByteBuffer var0, long var1);

    private static native byte[][] secp256k1_ec_pubkey_parse(ByteBuffer var0, long var1, int var3);

    private static native byte[][] secp256k1_schnorr_sign(ByteBuffer var0, long var1);

    private static native byte[][] secp256k1_ecdh(ByteBuffer var0, long var1, int var3);

    static {
        R_LOCK = RWL.readLock();
        nativeECDSABuffer = new ThreadLocal();
    }
}