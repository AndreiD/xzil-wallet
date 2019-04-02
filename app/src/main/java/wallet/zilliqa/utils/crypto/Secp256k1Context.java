package wallet.zilliqa.utils.crypto;

import java.security.AccessControlException;

public class Secp256k1Context {
    private static final boolean ENABLED;
    private static final long CONTEXT;

    public Secp256k1Context() {
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static long getContext() {
        return !ENABLED ? -1L : CONTEXT;
    }

    private static native long secp256k1_init_context();

    static {
        boolean isEnabled = true;
        long contextRef = -1L;

        try {
            System.loadLibrary("secp256k1");
            contextRef = secp256k1_init_context();
        } catch (UnsatisfiedLinkError var4) {
            isEnabled = false;
        } catch (AccessControlException var5) {
            isEnabled = false;
        }

        ENABLED = isEnabled;
        CONTEXT = contextRef;
    }
}
