package wallet.zilliqa.utils.crypto;



import java.io.*;
import java.security.Provider;
import java.security.SecureRandomSpi;
import java.security.Security;

public class LinuxSecureRandom extends SecureRandomSpi {
    private static final FileInputStream U_RANDOM;
    private final DataInputStream dis;

    public LinuxSecureRandom() {
        this.dis = new DataInputStream(U_RANDOM);
    }

    protected void engineSetSeed(byte[] bytes) {
    }

    protected void engineNextBytes(byte[] bytes) {
        try {
            this.dis.readFully(bytes);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    protected byte[] engineGenerateSeed(int i) {
        byte[] bits = new byte[i];
        this.engineNextBytes(bits);
        return bits;
    }

    static {
        try {
            File file = new File("/dev/urandom");
            U_RANDOM = new FileInputStream(file);
            if (U_RANDOM.read() == -1) {
                throw new RuntimeException("/dev/urandom not readable?");
            } else {
                int position = Security.insertProviderAt(new LinuxSecureRandomProvider(), 1);
                if (position != -1) {
//                    log.info("Secure randomness will be read from {} only.", file);
                } else {
//                    log.info("Randomness is already secure.");
                }

            }
        } catch (FileNotFoundException var2) {
            throw new RuntimeException("/dev/urandom does not appear to exist or is not openable",var2);
        } catch (IOException var3) {
            throw new RuntimeException("/dev/urandom does not appear to be readable",var3);
        }
    }

    private static class LinuxSecureRandomProvider extends Provider {
        public LinuxSecureRandomProvider() {
            super("LinuxSecureRandom", 1.0D, "A Linux specific random number provider that uses /dev/urandom");
            this.put("SecureRandom.LinuxSecureRandom", LinuxSecureRandom.class.getName());
        }
    }
}
