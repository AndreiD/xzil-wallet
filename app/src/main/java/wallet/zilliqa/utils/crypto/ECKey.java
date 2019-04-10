package wallet.zilliqa.utils.crypto;


import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.spongycastle.asn1.*;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.*;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;
import org.spongycastle.math.ec.FixedPointUtil;
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve;
import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;


public class ECKey {
    private static final String COMMON_CURVE = "secp256k1";
    private static final byte ADDRESS_HEADER_TEST = 0;
    private static final byte ADDRESS_HEADER_NORMAL = 1;
    private static final byte ADDRESS_HEADER_MULTISIG = 5;
    private static final ECDomainParameters CURVE;
    private static final BigInteger HALF_CURVE_ORDER;
    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    private static final SecureRandom SECURE_RANDOM;
    protected final BigInteger priv;
    protected final LazyECASPoint pub;
    protected KeyPair keyPair;
    protected long creationTimeSeconds;

    public ECKey() {
        this(SECURE_RANDOM);
    }

    public ECKey(SecureRandom secureRandom) {
        if (null == secureRandom) {
            throw new IllegalArgumentException("secureRandom is null");
        } else {
            this.keyPair = new KeyPair();
            ECKeyPairGenerator generator = new ECKeyPairGenerator();
            ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, secureRandom);
            generator.init(keygenParams);
            AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
            ECPrivateKeyParameters privParams = (ECPrivateKeyParameters)keypair.getPrivate();
            ECPublicKeyParameters pubParams = (ECPublicKeyParameters)keypair.getPublic();
            this.priv = privParams.getD();
            this.pub = new LazyECASPoint(CURVE.getCurve(), pubParams.getQ().getEncoded(true));
            this.creationTimeSeconds = CryptoUtils.currentTimeSeconds();
            this.keyPair.setPriKey(this.getPrivateKeyAsHex());
            this.keyPair.setPubKey(this.getPublicKeyAsHex());
        }
    }

    public ECKey(KeyPair keyPair, boolean compressed) {
        if (null == keyPair) {
            throw new IllegalArgumentException("params is null");
        } else {
            this.keyPair = new KeyPair();
            if (keyPair.getPriKey() == null && keyPair.getPubKey() == null) {
                throw new IllegalArgumentException("KeyPair requires at least private or public key");
            } else {
                this.priv = new BigInteger(1, CryptoUtils.HEX.decode(keyPair.getPriKey()));
                if (keyPair.getPubKey() == null) {
                    ECPoint point = publicPointFromPrivateKey(this.priv);
                    point = getPointWithCompression(point, compressed);
                    this.pub = new LazyECASPoint(point);
                } else {
                    this.pub = new LazyECASPoint(CURVE.getCurve(), CryptoUtils.HEX.decode(keyPair.getPubKey()));
                }

                this.keyPair.setPriKey(this.getPrivateKeyAsHex());
                this.keyPair.setPubKey(this.getPublicKeyAsHex());
            }
        }
    }

    private ECKey(BigInteger priv, ECPoint pub) {
        this(priv, new LazyECASPoint((ECPoint) Preconditions.checkNotNull(pub)));
    }

    private ECKey(BigInteger priv, LazyECASPoint pub) {
        if (null == pub) {
            throw new IllegalArgumentException("LazyECASPoint is null");
        } else {
            this.keyPair = new KeyPair();
            this.priv = priv;
            this.pub = (LazyECASPoint) Preconditions.checkNotNull(pub);
            if (priv != null) {
                Preconditions.checkArgument(priv.bitLength() <= 256, "private key exceeds 32 bytes: %s bits", new Object[]{priv.bitLength()});
                Preconditions.checkArgument(!priv.equals(BigInteger.ZERO));
                Preconditions.checkArgument(!priv.equals(BigInteger.ONE));
                this.keyPair.setPriKey(this.getPrivateKeyAsHex());
                this.keyPair.setPubKey(this.getPublicKeyAsHex());
            }

        }
    }

    public static ECKey fromASN1Key(byte[] asn1privkey) {
        return extractKeyFromASN1(asn1privkey);
    }

    public static ECPoint publicPointFromPrivateKey(BigInteger privKey) {
        if (null == privKey) {
            return null;
        } else {
            if (privKey.bitLength() > CURVE.getN().bitLength()) {
                privKey = privKey.mod(CURVE.getN());
            }

            return (new FixedPointCombMultiplier()).multiply(CURVE.getG(), privKey);
        }
    }

    public static ECKey fromPrivateKey(KeyPair key) {
        return null != key && null != key.getPriKey() ? fromPrivateKey(CryptoUtils.HEX.decode(key.getPriKey())) : null;
    }

    public static ECKey fromPrivateKey(String priKey) {
        return null == priKey ? null : fromPrivateKey(CryptoUtils.HEX.decode(priKey));
    }

    public static ECKey fromPublicKeyOnly(KeyPair key) {
        return null != key && null != key.getPubKey() ? fromPublicKeyOnly(CryptoUtils.HEX.decode(key.getPubKey())) : null;
    }

    public static ECKey fromPublicKeyOnly(String pubKey) {
        return null == pubKey ? null : fromPublicKeyOnly(CryptoUtils.HEX.decode(pubKey));
    }

    public static boolean checkPublicKey(KeyPair key) {
        if (null != key && null != key.getPubKey()) {
            try {
                fromPublicKeyOnly(CryptoUtils.HEX.decode(key.getPubKey()));
                return true;
            } catch (Exception var2) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean checkBase58Addr(String address) {
        if (null == address) {
            return false;
        } else {
            try {
                Base58.decodeChecked(address);
                return true;
            } catch (Exception var2) {
                return false;
            }
        }
    }

    public static boolean checkPubKeyAndAddr(String pubKey, String addr) {
        if (null != pubKey && null != addr) {
            String address = pubKey2Base58Address(pubKey);
            return addr.equals(address);
        } else {
            return false;
        }
    }

    public static boolean checkPriKeyAndPubKey(KeyPair key) {
        if (null == key) {
            return false;
        } else if (null != key.getPubKey() && null != key.getPriKey()) {
            ECKey newKey = fromPrivateKey(key);
            return null == newKey ? false : key.getPubKey().equals(newKey.getPublicKeyAsHex());
        } else {
            return false;
        }
    }

    public static boolean checkPriKeyAndPubKey(String priKey, String pubKey) {
        if (null != pubKey && null != priKey) {
            ECKey newKey = fromPrivateKey(priKey);
            return null == newKey ? false : pubKey.equals(newKey.getPublicKeyAsHex());
        } else {
            return false;
        }
    }

    public static String pubKey2Base58Address(KeyPair key) {
        if (null != key && null != key.getPubKey()) {
            byte[] hash160 = CryptoUtils.sha256hash160(CryptoUtils.HEX.decode(key.getPubKey()));
            if (null == hash160) {
                return null;
            } else {
                Preconditions.checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");
                byte[] addressBytes = new byte[1 + hash160.length + 4];
                addressBytes[0] = 0;
                System.arraycopy(hash160, 0, addressBytes, 1, hash160.length);
                byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, hash160.length + 1);
                System.arraycopy(checksum, 0, addressBytes, hash160.length + 1, 4);
                return Base58.encode(addressBytes);
            }
        } else {
            return null;
        }
    }

    public static String pubKey2Base58Address(String pubKey) {
        if (null == pubKey) {
            return null;
        } else {
            byte[] hash160 = CryptoUtils.sha256hash160(CryptoUtils.HEX.decode(pubKey));
            if (null == hash160) {
                return null;
            } else {
                Preconditions.checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");
                byte[] addressBytes = new byte[1 + hash160.length + 4];
                addressBytes[0] = 0;
                System.arraycopy(hash160, 0, addressBytes, 1, hash160.length);
                byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, hash160.length + 1);
                System.arraycopy(checksum, 0, addressBytes, hash160.length + 1, 4);
                return Base58.encode(addressBytes);
            }
        }
    }

    public static boolean isPublicKeyCanonical(KeyPair key) {
        if (null != key && null != key.getPubKey()) {
            byte[] pubkey = CryptoUtils.HEX.decode(key.getPubKey());
            if (pubkey.length < 33) {
                return false;
            } else if (pubkey[0] == 4) {
                return pubkey.length == 65;
            } else if (pubkey[0] != 2 && pubkey[0] != 3) {
                return false;
            } else {
                return pubkey.length == 33;
            }
        } else {
            return false;
        }
    }

    public static boolean isPublicKeyCanonical(String pubKey) {
        if (null == pubKey) {
            return false;
        } else {
            byte[] key = CryptoUtils.HEX.decode(pubKey);
            if (key.length < 33) {
                return false;
            } else if (key[0] == 4) {
                return key.length == 65;
            } else if (key[0] != 2 && key[0] != 3) {
                return false;
            } else {
                return key.length == 33;
            }
        }
    }

    public static ECKey recoverFromSignature(int recId, ECASSignature sig, Sha256Hash message, boolean compressed) {
        Preconditions.checkArgument(recId >= 0, "recId must be positive");
        Preconditions.checkArgument(sig.r.signum() >= 0, "r must be positive");
        Preconditions.checkArgument(sig.s.signum() >= 0, "s must be positive");
        Preconditions.checkNotNull(message);
        BigInteger n = CURVE.getN();
        BigInteger i = BigInteger.valueOf((long)recId / 2L);
        BigInteger x = sig.r.add(i.multiply(n));
        BigInteger prime = SecP256K1Curve.q;
        if (x.compareTo(prime) >= 0) {
            return null;
        } else {
            ECPoint point = decompressKey(x, (recId & 1) == 1);
            if (!point.multiply(n).isInfinity()) {
                return null;
            } else {
                BigInteger e = message.toBigInteger();
                BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
                BigInteger rInv = sig.r.modInverse(n);
                BigInteger srInv = rInv.multiply(sig.s).mod(n);
                BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
                ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, point, srInv);
                return fromPublicKeyOnly(q.getEncoded(compressed));
            }
        }
    }

    public static boolean verifySign(String message, String signature, String pubkey) {
        if (null != message && null != signature && null != pubkey) {
            ECKey ecKey = fromPublicKeyOnly(CryptoUtils.HEX.decode(pubkey));
            if (null == ecKey) {
                return false;
            } else {
                try {
                    ecKey.verify(message, signature);
                    return true;
                } catch (SignatureException var5) {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
        if (Secp256k1Context.isEnabled()) {
            try {
                return NativeSecp256k1.verify(data, signature, pub);
            } catch (Exception var4) {
//                log.error("Caught AssertFailException inside secp256k1", var4);
                return false;
            }
        } else {
            return verify(data, ECASSignature.decodeFromDER(signature), pub);
        }
    }

    public static String createMultiSigByBase58(int sigNum, List<String> pubKeyLists) {
        if (pubKeyLists !=null && !pubKeyLists.isEmpty() && 0 < sigNum) {
            if (sigNum > pubKeyLists.size()) {
                throw new IllegalArgumentException("sigNum cannot large pubKeyLists.size()");
            } else if (sigNum > 21) {
                throw new IllegalArgumentException("sigNum is too large than 21");
            } else {
                int length = pubKeyLists.size();
                byte[] array = new byte[1 + length * 33];
                array[0] = (byte)sigNum;

                for(int i = 0; i < pubKeyLists.size(); ++i) {
                    byte[] pubKeyByte = CryptoUtils.HEX.decode((CharSequence)pubKeyLists.get(i));
                    System.arraycopy(pubKeyByte, 0, array, i * 33 + 1, pubKeyByte.length);
                }

                byte[] hash160 = CryptoUtils.sha256hash160(array);
                Preconditions.checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");
                byte[] addressBytes = new byte[1 + hash160.length + 4];
                addressBytes[0] = 5;
                System.arraycopy(hash160, 0, addressBytes, 1, hash160.length);
                byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, hash160.length + 1);
                System.arraycopy(checksum, 0, addressBytes, hash160.length + 1, 4);
                return Base58.encode(addressBytes);
            }
        } else {
            throw new IllegalArgumentException("params is error");
        }
    }

    public static String signMessage(String message, String priKey) {
        if (null != message && null != priKey) {
            ECKey eckey = fromPrivateKey(priKey);
            if (null == eckey) {
                return null;
            } else {
                byte[] data = CryptoUtils.formatMessageForSigning(message, Charset.forName("UTF-8"), (byte[])null);
                Sha256Hash hash = Sha256Hash.twiceOf(data);
                return eckey.signMessage((Sha256Hash)hash, (KeyParameter)null);
            }
        } else {
            String error = "params is null";
            throw new IllegalArgumentException(error);
        }
    }

    private static boolean verify(byte[] data, ECASSignature signature, byte[] pub) {
        if (Secp256k1Context.isEnabled()) {
            try {
                return NativeSecp256k1.verify(data, signature.encodeToDER(), pub);
            } catch (Exception var6) {
//                log.error("Caught AssertFailException inside secp256k1", var6);
                return false;
            }
        } else {
            ECDSASigner signer = new ECDSASigner();
            ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
            signer.init(false, params);

            try {
                return signer.verifySignature(data, signature.r, signature.s);
            } catch (NullPointerException var7) {
//                log.error("Caught NPE inside bouncy castle", var7);
                return false;
            }
        }
    }

    private static ECKey signedMessageToKey(String message, String signatureBase64) throws SignatureException {
        if (null != message && null != signatureBase64) {
            byte[] signatureEncoded;
            try {
                signatureEncoded = Base64.decode(signatureBase64);
            } catch (RuntimeException var12) {
                throw new SignatureException("Could not decode base64", var12);
            }

            if (signatureEncoded.length < 65) {
                throw new SignatureException("Signature truncated, expected 65 bytes and got " + signatureEncoded.length);
            } else {

                int header = signatureEncoded[0] & 255;
                if (header >= 27 && header <= 34) {
                    BigInteger r = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 1, 33));
                    BigInteger s = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 33, 65));
                    ECASSignature sig = new ECASSignature(r, s);
                    byte[] messageBytes = CryptoUtils.formatMessageForSigning(message, Charset.forName("UTF-8"), (byte[])null);
                    Sha256Hash messageHash = Sha256Hash.twiceOf(messageBytes);
                    boolean compressed = false;
                    if (header >= 31) {
                        compressed = true;
                        header -= 4;
                    }

                    int recId = header - 27;
                    ECKey key = recoverFromSignature(recId, sig, messageHash, compressed);
                    if (key == null) {
                        throw new SignatureException("Could not recover public key from signature");
                    } else {
                        return key;
                    }
                } else {
                    throw new SignatureException("Header byte out of range: " + header);
                }
            }
        } else {
            return null;
        }
    }

    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        if (null == xBN) {
            return null;
        } else {
            X9IntegerConverter x9 = new X9IntegerConverter();
            byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
            compEnc[0] = (byte)(yBit ? 3 : 2);
            return CURVE.getCurve().decodePoint(compEnc);
        }
    }

    private static ECKey extractKeyFromASN1(byte[] asn1privkey) {
        if (null == asn1privkey) {
            return null;
        } else {
            try {
                ASN1InputStream decoder = new ASN1InputStream(asn1privkey);
                DLSequence seq = (DLSequence)decoder.readObject();
                Preconditions.checkArgument(decoder.readObject() == null, "Input contains extra bytes");
                decoder.close();
                Preconditions.checkArgument(seq.size() == 4, "Input does not appear to be an ASN.1 OpenSSL EC private key");
                Preconditions.checkArgument(((ASN1Integer)seq.getObjectAt(0)).getValue().equals(BigInteger.ONE), "Input is of wrong version");
                byte[] privbits = ((ASN1OctetString)seq.getObjectAt(1)).getOctets();
                BigInteger privkey = new BigInteger(1, privbits);
                ASN1TaggedObject pubkey = (ASN1TaggedObject)seq.getObjectAt(3);
                Preconditions.checkArgument(pubkey.getTagNo() == 1, "Input has 'publicKey' with bad tag number");
                byte[] pubbits = ((DERBitString)pubkey.getObject()).getBytes();
                Preconditions.checkArgument(pubbits.length == 33 || pubbits.length == 65, "Input has 'publicKey' with invalid length");
                int encoding = pubbits[0] & 255;
                Preconditions.checkArgument(encoding >= 2 && encoding <= 4, "Input has 'publicKey' with invalid encoding");
                boolean compressed = pubbits.length == 33;
                KeyPair pair = new KeyPair();
                pair.setPriKey(privkey.toString());
                ECKey key = new ECKey(pair, compressed);
                if (!Arrays.equals(key.getPublicKey(), pubbits)) {
                    throw new IllegalArgumentException("Public key in ASN.1 structure does not match private key.");
                } else {
                    return key;
                }
            } catch (IOException var11) {
                throw new RuntimeException(var11);
            }
        }
    }

    private static ECKey fromPrivateKey(byte[] privKeyBytes) {
        return null == privKeyBytes ? null : fromPrivateKey(new BigInteger(1, privKeyBytes));
    }

    private static ECKey fromPrivateKey(BigInteger privKey) {
        return null == privKey ? null : fromPrivateKey(privKey, true);
    }

    private static ECKey fromPrivateKey(BigInteger privKey, boolean compressed) {
        if (null == privKey) {
            return null;
        } else {
            ECPoint point = publicPointFromPrivateKey(privKey);
            return new ECKey(privKey, getPointWithCompression(point, compressed));
        }
    }

    private static ECKey fromPrivateKey(byte[] privKeyBytes, boolean compressed) {
        return null == privKeyBytes ? null : fromPrivateKey(new BigInteger(1, privKeyBytes), compressed);
    }

    private static ECKey fromPublicKeyOnly(byte[] pub) {
        return null == pub ? null : new ECKey((BigInteger)null, CURVE.getCurve().decodePoint(pub));
    }

    private static ECPoint getPointWithCompression(ECPoint point, boolean compressed) {
        if (null == point) {
            return null;
        } else if (point.isCompressed() == compressed) {
            return point;
        } else {
            point = point.normalize();
            BigInteger x = point.getAffineXCoord().toBigInteger();
            BigInteger y = point.getAffineYCoord().toBigInteger();
            return CURVE.getCurve().createPoint(x, y, compressed);
        }
    }

    public boolean verify(String hash, String signatureBase58) throws SignatureException {
        byte[] signatureEncoded;
        try {
            signatureEncoded = Base58.decode(signatureBase58);
        } catch (Throwable var12) {
            throw new SignatureException("Could not decode base64", var12);
        }
        return verify(hash.getBytes(), signatureEncoded, this.getPublicKey());
    }

    public boolean verify(byte[] hash, byte[] signature) {
        return verify(hash, signature, this.getPublicKey());
    }

    public BigInteger getPrivateKeyByBigInteger() {
        if (this.priv == null) {
            throw new MissingPrivateKeyException();
        } else {
            return this.priv;
        }
    }

    public String signMessage(String message) {
        if (message == null) {
            String error = "params is null";
            throw new IllegalArgumentException(error);
        } else {
            byte[] data = CryptoUtils.formatMessageForSigning(message, Charset.forName("UTF-8"), (byte[])null);
            Sha256Hash hash = Sha256Hash.twiceOf(message.getBytes());
//            Sha256Hash hash = Sha256Hash.of(message.getBytes());
            return this.signMessage((Sha256Hash)hash, (KeyParameter)null);
        }
    }

    public boolean isCompressed() {
        return this.pub.isCompressed();
    }

    private byte[] getPrivateKeyBytes() {
        return CryptoUtils.bigIntegerToBytes(this.getPrivateKeyByBigInteger(), 32);
    }

    public byte[] getPublicKey() {
        return this.pub.getEncoded();
    }

    private byte[] getPublicKeyHash() {
        return CryptoUtils.sha256hash160(this.pub.getEncoded());
    }

    private String signMessage(Sha256Hash messageHash, KeyParameter aesKey) {
        if (null == messageHash) {
            String error = "params is null";
            throw new IllegalArgumentException(error);
        } else {
            ECASSignature sig = this.sign(messageHash, aesKey);
//            System.out.println(CryptoUtils.HEX.encode(sig.encodeToDER()));

            return Base58.encode(sig.encodeToDER());
//            int recId = -1;
//
//            int headerByte;
//            for(headerByte = 0; headerByte < 4; ++headerByte) {
//                ECKey k = recoverFromSignature(headerByte, sig, messageHash, this.isCompressed());
//                if (k != null && k.pub.equals(this.pub)) {
//                    recId = headerByte;
//                    break;
//                }
//            }
//
//            if (recId == -1) {
//                throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
//            } else {
//                headerByte = recId + 27 + (this.isCompressed() ? 4 : 0);
//                byte[] sigData = new byte[65];
//                sigData[0] = (byte)headerByte;
//                System.arraycopy(CryptoUtils.bigIntegerToBytes(sig.r, 32), 0, sigData, 1, 32);
//                System.arraycopy(CryptoUtils.bigIntegerToBytes(sig.s, 32), 0, sigData, 33, 32);
//                return new String(Base64.encode(sigData), Charset.forName("UTF-8"));
//            }
        }
    }

    private ECASSignature sign(Sha256Hash input, KeyParameter aesKey) {
        return this.doSign(input, this.priv);
    }

    private ECASSignature doSign(Sha256Hash input, BigInteger privateKeyForSigning) {
        if (Secp256k1Context.isEnabled()) {
            try {
                byte[] signature = NativeSecp256k1.sign(input.getBytes(), CryptoUtils.bigIntegerToBytes(privateKeyForSigning, 32));
                return ECASSignature.decodeFromDER(signature);
            } catch (Exception var6) {
//                log.error("Caught AssertFailException inside secp256k1", var6);
                throw new RuntimeException(var6);
            }
        } else {
            ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
            ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKeyForSigning, CURVE);
            signer.init(true, privKey);
            BigInteger[] components = signer.generateSignature(input.getBytes());
            return (new ECASSignature(components[0], components[1])).toCanonicalised();
        }
    }

    public String getPrivateKeyAsHex() {
        return CryptoUtils.HEX.encode(this.getPrivateKeyBytes());
    }

    public String toBase58Address() {
        byte[] hash160 = this.getPublicKeyHash();
        Preconditions.checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");
        byte[] addressBytes = new byte[1 + hash160.length + 4];
        addressBytes[0] = 0;
        System.arraycopy(hash160, 0, addressBytes, 1, hash160.length);
        byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, hash160.length + 1);
        System.arraycopy(checksum, 0, addressBytes, hash160.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    public boolean verifySign(String message, String signature) {
        if (null != message && null != signature) {
            try {
                this.verifyMessage(message, signature);
                return true;
            } catch (SignatureException var4) {
                return false;
            }
        } else {
            return false;
        }
    }

    private void verifyMessage(String message, String signatureBase64) throws SignatureException {
        ECKey key = signedMessageToKey(message, signatureBase64);
        if (null == key) {
            throw new SignatureException("cannot produce key");
        } else if (!key.pub.equals(this.pub)) {
            throw new SignatureException("Signature did not match for message");
        }
    }

    public String getPublicKeyAsHex() {
        return CryptoUtils.HEX.encode(this.pub.getEncoded());
    }

    public KeyPair getKeyPair() {
        return this.keyPair;
    }

    static {
        if (CryptoUtils.isAndroidRuntime()) {
            new LinuxSecureRandom();
        }

        FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
        CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
        HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
        SECURE_RANDOM = new SecureRandom();
    }

    public static class ECASSignature {
        public final BigInteger r;
        public final BigInteger s;

        public ECASSignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        public static ECASSignature decodeFromDER(byte[] bytes) throws IllegalArgumentException {
            if (null == bytes) {
                return null;
            } else {
                ASN1InputStream decoder = null;

                ECASSignature var6;
                try {
                    decoder = new ASN1InputStream(bytes);
                    ASN1Primitive seqObj = decoder.readObject();
                    if (seqObj == null) {
                        throw new IllegalArgumentException("Reached past end of ASN.1 stream.");
                    }

                    if (!(seqObj instanceof DLSequence)) {
                        throw new IllegalArgumentException("Read unexpected class: " + seqObj.getClass().getName());
                    }

                    DLSequence seq = (DLSequence)seqObj;

                    ASN1Integer r;
                    ASN1Integer s;
                    try {
                        r = (ASN1Integer)seq.getObjectAt(0);
                        s = (ASN1Integer)seq.getObjectAt(1);
                    } catch (ClassCastException var16) {
                        throw new IllegalArgumentException(var16);
                    }

                    var6 = new ECASSignature(r.getPositiveValue(), s.getPositiveValue());
                } catch (IOException var17) {
                    throw new IllegalArgumentException(var17);
                } finally {
                    if (decoder != null) {
                        try {
                            decoder.close();
                        } catch (IOException ignored) {
                        }
                    }

                }

                return var6;
            }
        }

        public boolean isCanonical() {
            return this.s.compareTo(ECKey.HALF_CURVE_ORDER) <= 0;
        }

        public ECASSignature toCanonicalised() {
            return !this.isCanonical() ? new ECASSignature(this.r, ECKey.CURVE.getN().subtract(this.s)) : this;
        }

        public byte[] encodeToDER() {
            try {
                return this.derByteStream().toByteArray();
            } catch (IOException var2) {
                throw new RuntimeException(var2);
            }
        }

        protected ByteArrayOutputStream derByteStream() throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
            DERSequenceGenerator seq = new DERSequenceGenerator(bos);
            seq.addObject(new ASN1Integer(this.r));
            seq.addObject(new ASN1Integer(this.s));
            seq.close();
            return bos;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                ECASSignature other = (ECASSignature)o;
                return this.r.equals(other.r) && this.s.equals(other.s);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hashCode(new Object[]{this.r, this.s});
        }
    }

    public static class KeyIsEncryptedException extends MissingPrivateKeyException {
        public KeyIsEncryptedException() {
        }
    }

    public static class MissingPrivateKeyException extends RuntimeException {
        public MissingPrivateKeyException() {
        }
    }

}
