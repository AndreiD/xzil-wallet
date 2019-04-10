package wallet.zilliqa.utils.crypto;

import android.support.annotation.NonNull;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Sha256Hash implements Serializable, Comparable<Sha256Hash> {
    public static final int LENGTH = 32;
    public static final Sha256Hash ZERO_HASH = wrap(new byte[32]);
    private final byte[] bytes;

    private Sha256Hash(byte[] rawHashBytes) {
        Preconditions.checkArgument(rawHashBytes.length == 32);
        this.bytes = rawHashBytes;
    }

    public static Sha256Hash wrap(String hexString) {
        return wrap(CryptoUtils.HEX.decode(hexString));
    }

    public static Sha256Hash wrapReversed(byte[] rawHashBytes) {
        return wrap(CryptoUtils.reverseBytes(rawHashBytes));
    }

    public static Sha256Hash of(byte[] contents) {
        return wrap(hash(contents));
    }

    public static Sha256Hash twiceOf(byte[] contents) {
        return wrap(hashTwice(contents));
    }

    public static Sha256Hash of(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);

        Sha256Hash var2;
        try {
            var2 = of(ByteStreams.toByteArray(in));
        } finally {
            in.close();
        }

        return var2;
    }

    public static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException var1) {
            throw new IllegalStateException(var1);
        }
    }

    public static byte[] hash(byte[] input) {
        return hash(input, 0, input.length);
    }

    public static byte[] hash(byte[] input, int offset, int length) {
        MessageDigest digest = newDigest();
        digest.update(input, offset, length);
        return digest.digest();
    }

    public static byte[] hashTwice(byte[] input) {
        return hashTwice(input, 0, input.length);
    }

    public static byte[] hashTwice(byte[] input, int offset, int length) {
        MessageDigest digest = newDigest();
        digest.update(input, offset, length);
        return digest.digest(digest.digest());
    }

    public static byte[] hashTwice(byte[] input1, int offset1, int length1, byte[] input2, int offset2, int length2) {
        MessageDigest digest = newDigest();
        digest.update(input1, offset1, length1);
        digest.update(input2, offset2, length2);
        return digest.digest(digest.digest());
    }

    private static Sha256Hash wrap(byte[] rawHashBytes) {
        return new Sha256Hash(rawHashBytes);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            return o != null && this.getClass() == o.getClass() ? Arrays.equals(this.bytes, ((Sha256Hash)o).bytes) : false;
        }
    }

    public int hashCode() {
        return Ints.fromBytes(this.bytes[28], this.bytes[29], this.bytes[30], this.bytes[31]);
    }

    public String toString() {
        return CryptoUtils.HEX.encode(this.bytes);
    }

    public BigInteger toBigInteger() {
        return new BigInteger(1, this.bytes);
    }

    public byte[] getReversedBytes() {
        return CryptoUtils.reverseBytes(this.bytes);
    }

    public int compareTo(@NonNull Sha256Hash other) {
        for(int i = 31; i >= 0; --i) {
            int thisByte = this.bytes[i] & 255;
            int otherByte = other.bytes[i] & 255;
            if (thisByte > otherByte) {
                return 1;
            }

            if (thisByte < otherByte) {
                return -1;
            }
        }

        return 0;
    }

    public byte[] getBytes() {
        return this.bytes;
    }
}