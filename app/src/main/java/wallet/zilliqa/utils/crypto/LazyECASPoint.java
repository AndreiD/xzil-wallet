package wallet.zilliqa.utils.crypto;

import com.google.common.base.Preconditions;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;

import java.util.Arrays;

/**
 * @author fanyongpeng
 * @create 2018-08-13 11:32
 **/
public class LazyECASPoint {
    private final ECCurve curve;
    private final byte[] bits;
    private ECPoint point;

    public LazyECASPoint(ECCurve curve, byte[] bits) {
        this.curve = curve;
        this.bits = bits;
    }

    public LazyECASPoint(ECPoint point) {
        this.point = (ECPoint) Preconditions.checkNotNull(point);
        this.curve = null;
        this.bits = null;
    }

    public ECPoint get() {
        if (this.point == null) {
            this.point = this.curve.decodePoint(this.bits);
        }

        return this.point;
    }

    public byte[] getEncoded() {
        return this.bits != null ? Arrays.copyOf(this.bits, this.bits.length) : this.get().getEncoded();
    }

    public boolean isCompressed() {
        if (this.bits == null) {
            return this.get().isCompressed();
        } else {
            return this.bits[0] == 2 || this.bits[0] == 3;
        }
    }

    public boolean equals(ECPoint other) {
        return this.get().equals(other);
    }

    public byte[] getEncoded(boolean compressed) {
        return compressed == this.isCompressed() && this.bits != null ? Arrays.copyOf(this.bits, this.bits.length) : this.get().getEncoded(compressed);
    }

    public ECPoint add(ECPoint b) {
        return this.get().add(b);
    }

    public ECPoint normalize() {
        return this.get().normalize();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            return o != null && this.getClass() == o.getClass() ? Arrays.equals(this.getCanonicalEncoding(), ((LazyECASPoint)o).getCanonicalEncoding()) : false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.getCanonicalEncoding());
    }

    private byte[] getCanonicalEncoding() {
        return this.getEncoded(true);
    }
}
