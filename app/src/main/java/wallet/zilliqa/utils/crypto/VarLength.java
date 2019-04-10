package wallet.zilliqa.utils.crypto;


public class VarLength {
    private static final long FIRST_VAR_LENGTH_ONE = 253L;
    private static final long FIRST_VAR_LENGTH_TWO = 254L;
    private static final long MIDDLE_SIZE = 65535L;
    private static final long MAX_SIZE = 65535L;
    public final long value;
    private final int originallyEncodedSize;

    public VarLength(long value) {
        this.value = value;
        this.originallyEncodedSize = this.getSizeInBytes();
    }

    public VarLength(byte[] buf, int offset) {
        int first = 255 & buf[offset];
        if ((long)first < 253L) {
            this.value = (long)first;
            this.originallyEncodedSize = 1;
        } else if ((long)first == 253L) {
            this.value = (long)(255 & buf[offset + 1] | (255 & buf[offset + 2]) << 8);
            this.originallyEncodedSize = 3;
        } else if ((long)first == 254L) {
            this.value = CryptoUtils.readUint32(buf, offset + 1);
            this.originallyEncodedSize = 5;
        } else {
            this.value = CryptoUtils.readInt64(buf, offset + 1);
            this.originallyEncodedSize = 9;
        }

    }

    public static int sizeOf(long value) {
        if (value < 0L) {
            return 9;
        } else if (value < 253L) {
            return 1;
        } else if (value <= 65535L) {
            return 3;
        } else {
            return 9;
        }
    }

    public byte[] encode() {
        byte[] bytes;
        switch(sizeOf(this.value)) {
            case 1:
                return new byte[]{(byte)((int)this.value)};
            case 2:
            case 4:
            default:
                bytes = new byte[9];
                bytes[0] = -1;
                CryptoUtils.uint64ToByteArrayLE(this.value, bytes, 1);
                return bytes;
            case 3:
                return new byte[]{-3, (byte)((int)this.value), (byte)((int)(this.value >> 8))};
            case 5:
                bytes = new byte[5];
                bytes[0] = -2;
                CryptoUtils.uint32ToByteArrayLE(this.value, bytes, 1);
                return bytes;
        }
    }

    public final int getSizeInBytes() {
        return sizeOf(this.value);
    }
}
