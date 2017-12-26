package db;

/**
 * Created by Ethan Hu on 3/4/2017.
 */
public class ArithmeticOps<T extends Number> {
    public boolean checkNaN(T x, T y) {
        if (x.floatValue() == Float.NaN || y.floatValue() == Float.NaN) {
            return true;
        }
        return false;
    }

    public String strConcat(String x, String y) {
        x = x.substring(0,x.length() - 1);
        y = y.substring(1, y.length());
        return x + y;
    }

    //Only use below for mixed arithmetic operations.
    public Float mAdd(T x, T y) {
        if (checkNaN(x, y)) {
            return Float.NaN;
        }
        return x.floatValue() + y.floatValue();
    }

    public Float mMultiply(T x, T y) {
        if (checkNaN(x, y)) {
            return Float.NaN;
        }
        return x.floatValue() * y.floatValue();
    }

    public Float mDivide(T x, T y) {
        if (checkNaN(x, y)) {
            return Float.NaN;
        }
        return x.floatValue() / y.floatValue();
    }

    public Float mSubtract(T x, T y) {
        if (checkNaN(x, y)) {
            return Float.NaN;
        }
        return x.floatValue() - y.floatValue();
    }

    public Integer add(T x, T y) {
        return x.intValue() + y.intValue();
    }

    public Integer multiply(T x, T y) {
        return x.intValue() * y.intValue();
    }

    public Integer divide(T x, T y) {
        return x.intValue() / y.intValue();
    }

    public Integer subtract(T x, T y) {
        return x.intValue() - y.intValue();
    }


}
