package wallet.zilliqa.utils.crypto;

import java.math.BigInteger;
import java.util.Arrays;

public class Base58 {
    private static final int LENGTH = 4;
    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final char ENCODED_ZERO;
    private static final int[] INDEXES;

    private Base58() {
    }

    public static String encode(byte[] input) {
        if (input.length == 0) {
            return "";
        } else {
            int zeros;
            for(zeros = 0; zeros < input.length && input[zeros] == 0; ++zeros) {
            }

            char[] encoded = new char[input.length * 2];
            int outputStart = encoded.length;
            int inputStart = zeros;

            while(inputStart < input.length) {
                --outputStart;
                encoded[outputStart] = ALPHABET[divmod(input, inputStart, 256, 58)];
                if (input[inputStart] == 0) {
                    ++inputStart;
                }
            }

            while(outputStart < encoded.length && encoded[outputStart] == ENCODED_ZERO) {
                ++outputStart;
            }

            while(true) {
                --zeros;
                if (zeros < 0) {
                    return new String(encoded, outputStart, encoded.length - outputStart);
                }

                --outputStart;
                encoded[outputStart] = ENCODED_ZERO;
            }
        }
    }

    public static byte[] decode(String input) {
        if (input.length() == 0) {
            return new byte[0];
        } else {
            byte[] input58 = new byte[input.length()];

            int zeros;
            int outputStart;
            for(zeros = 0; zeros < input.length(); ++zeros) {
                char c = input.charAt(zeros);
                outputStart = c < 128 ? INDEXES[c] : -1;
                if (outputStart < 0) {
                    throw new RuntimeException("Illegal character " + c + " at position " + zeros);
                }

                input58[zeros] = (byte)outputStart;
            }

            for(zeros = 0; zeros < input58.length && input58[zeros] == 0; ++zeros) {
            }

            byte[] decoded = new byte[input.length()];
            outputStart = decoded.length;
            int inputStart = zeros;

            while(inputStart < input58.length) {
                --outputStart;
                decoded[outputStart] = divmod(input58, inputStart, 58, 256);
                if (input58[inputStart] == 0) {
                    ++inputStart;
                }
            }

            while(outputStart < decoded.length && decoded[outputStart] == 0) {
                ++outputStart;
            }

            return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
        }
    }

    public static BigInteger decodeToBigInteger(String input) {
        return new BigInteger(1, decode(input));
    }

    public static byte[] decodeChecked(String input, IBase58CheckSumProvider checksumProvider) {
        byte[] decoded = decode(input);
        if (decoded.length < 4) {
            throw new RuntimeException("Input too short");
        } else {
            byte[] data = Arrays.copyOfRange(decoded, 0, decoded.length - 4);
            byte[] checksum = Arrays.copyOfRange(decoded, decoded.length - 4, decoded.length);
            byte[] actualChecksum = Arrays.copyOfRange(checksumProvider.calculateActualCheckSum(data), 0, 4);
            if (!Arrays.equals(checksum, actualChecksum)) {
                throw new RuntimeException("Checksum does not validate");
            } else {
                return data;
            }
        }
    }

    public static byte[] decodeChecked(String input) throws Exception {
        byte[] decoded = decode(input);
        if (decoded.length < 4) {
            throw new RuntimeException("Input too short");
        } else {
            byte[] data = Arrays.copyOfRange(decoded, 0, decoded.length - 4);
            byte[] checksum = Arrays.copyOfRange(decoded, decoded.length - 4, decoded.length);
            byte[] actualChecksum = Arrays.copyOfRange(Sha256Hash.hashTwice(data), 0, 4);
            if (!Arrays.equals(checksum, actualChecksum)) {
                throw new RuntimeException("Checksum does not validate");
            } else {
                return data;
            }
        }
    }

    private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
        int remainder = 0;

        for(int i = firstDigit; i < number.length; ++i) {
            int digit = number[i] & 255;
            int temp = remainder * base + digit;
            number[i] = (byte)(temp / divisor);
            remainder = temp % divisor;
        }

        return (byte)remainder;
    }

    static {
        ENCODED_ZERO = ALPHABET[0];
        INDEXES = new int[128];
        Arrays.fill(INDEXES, -1);

        for(int i = 0; i < ALPHABET.length; INDEXES[ALPHABET[i]] = i++) {
        }

    }
}