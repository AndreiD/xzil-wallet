package wallet.zilliqa.utils.crypto;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Date;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

/**
 * @author fanyongpeng
 * @create 2018-08-13 11:33
 **/
public class CryptoUtils {
    public static final String CASGLOBAL_SIGNED_HEADER = "CAS.global Signed Message:\n";
    public static final byte[] CASGLOBAL_SIGNED_HEADER_BYTES;
    public static final BaseEncoding HEX;
    public static volatile Date mockTime;
    private static int isAndroid;

    private CryptoUtils() {
    }

    public static boolean isAndroidRuntime() {
        if (isAndroid == -1) {
            String runtime = System.getProperty("java.runtime.name");
            isAndroid = runtime != null && "Android Runtime".equals(runtime) ? 1 : 0;
        }

        return isAndroid == 1;
    }

    public static long currentTimeSeconds() {
        return currentTimeMillis() / 1000L;
    }

    public static long currentTimeMillis() {
        return mockTime != null ? mockTime.getTime() : System.currentTimeMillis();
    }

    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        Preconditions.checkArgument(b.signum() >= 0, "b must be positive or zero");
        Preconditions.checkArgument(numBytes > 0, "numBytes must be positive");
        byte[] src = b.toByteArray();
        byte[] dest = new byte[numBytes];
        boolean isFirstByteOnlyForSign = src[0] == 0;
        int length = isFirstByteOnlyForSign ? src.length - 1 : src.length;
        Preconditions.checkArgument(length <= numBytes, "The given number does not fit in " + numBytes);
        int srcPos = isFirstByteOnlyForSign ? 1 : 0;
        int destPos = numBytes - length;
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }

    public static byte[] reverseBytes(byte[] bytes) {
        byte[] buf = new byte[bytes.length];

        for(int i = 0; i < bytes.length; ++i) {
            buf[i] = bytes[bytes.length - 1 - i];
        }

        return buf;
    }

    public static byte[] sha256hash160(byte[] input) {
        byte[] sha256 = Sha256Hash.hash(input);
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(sha256, 0, sha256.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
    }

    public static byte[] formatMessageForSigning(String message, Charset charset, byte[] headerBytes) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(CASGLOBAL_SIGNED_HEADER_BYTES.length);
            bos.write(CASGLOBAL_SIGNED_HEADER_BYTES);
            if (headerBytes != null && headerBytes.length > 0) {
                bos.write(headerBytes.length);
                bos.write(headerBytes);
            }

            byte[] messageBytes = message.getBytes(charset);
            VarLength size = new VarLength((long)messageBytes.length);
            bos.write(size.encode());
            bos.write(messageBytes);
            return bos.toByteArray();
        } catch (IOException var6) {
            throw new RuntimeException(var6);
        }
    }

    public static long readUint32(byte[] bytes, int offset) {
        return (long)bytes[offset] & 255L | ((long)bytes[offset + 1] & 255L) << 8 | ((long)bytes[offset + 2] & 255L) << 16 | ((long)bytes[offset + 3] & 255L) << 24;
    }

    public static long readInt64(byte[] bytes, int offset) {
        return (long)bytes[offset] & 255L | ((long)bytes[offset + 1] & 255L) << 8 | ((long)bytes[offset + 2] & 255L) << 16 | ((long)bytes[offset + 3] & 255L) << 24 | ((long)bytes[offset + 4] & 255L) << 32 | ((long)bytes[offset + 5] & 255L) << 40 | ((long)bytes[offset + 6] & 255L) << 48 | ((long)bytes[offset + 7] & 255L) << 56;
    }

    public static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
        out[offset] = (byte)((int)(255L & val >> 24));
        out[offset + 1] = (byte)((int)(255L & val >> 16));
        out[offset + 2] = (byte)((int)(255L & val >> 8));
        out[offset + 3] = (byte)((int)(255L & val));
    }

    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte)((int)(255L & val));
        out[offset + 1] = (byte)((int)(255L & val >> 8));
        out[offset + 2] = (byte)((int)(255L & val >> 16));
        out[offset + 3] = (byte)((int)(255L & val >> 24));
    }

    public static void uint64ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte)((int)(255L & val));
        out[offset + 1] = (byte)((int)(255L & val >> 8));
        out[offset + 2] = (byte)((int)(255L & val >> 16));
        out[offset + 3] = (byte)((int)(255L & val >> 24));
        out[offset + 4] = (byte)((int)(255L & val >> 32));
        out[offset + 5] = (byte)((int)(255L & val >> 40));
        out[offset + 6] = (byte)((int)(255L & val >> 48));
        out[offset + 7] = (byte)((int)(255L & val >> 56));
    }

    static {
        CASGLOBAL_SIGNED_HEADER_BYTES = "CAS.global Signed Message:\n".getBytes(Charsets.UTF_8);
        HEX = BaseEncoding.base16().lowerCase();
        isAndroid = -1;
    }
}
