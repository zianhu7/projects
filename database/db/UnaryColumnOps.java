package db;

/**
 * Created by Ethan Hu on 3/4/2017.
 */
public class UnaryColumnOps<T extends Comparable<? super T>> {
    private final TypeChecker tc = new TypeChecker();
    private final ArithmeticOps ao = new ArithmeticOps();

    public boolean checkUnaryValid(Column x, T literal, Character operator) {
        String xType = x.findType();
        String lType = tc.checkType(literal);
        if (xType.equals("string") && lType.equals("string")) {
            if (operator.equals('+')) {
                return true;
            } else {
                throw new RuntimeException("ERROR: Only addition is defined for operations on strings.");
            }
        }
        return true;
    }

    public boolean isIntOp(Column x, T y) {
        if (x.findType().equals("int") && tc.checkType(y).equals("int")) {
            return true;
        }
        return false;
    }

    public boolean isStrOp(Column x, T literal) {
        if (x.findType().equals("string") && tc.checkType(literal).equals("string")) {
            return true;
        }
        return false;
    }

    public boolean isFloatOp(Column x, T literal) {
        if (x.findType().equals("int") && tc.checkType(literal).equals("float")) {
            return true;
        } else if (x.findType().equals("float") && tc.checkType(literal).equals("int")) {
            return true;
        } else if (x.findType().equals("float") && tc.checkType(literal).equals("float")) {
            return true;
        } else {
            return false;
        }
    }

    public String newType(Column x, T literal) {
        if (isIntOp(x, literal)) {
            return "int";
        } else if (isStrOp(x, literal)) {
            return "string";
        } else if (isFloatOp(x, literal)) {
            return "float";
        } else {
            throw new RuntimeException("ERROR: Can't find the new type of combined columns from x & y, not defined.");
        }
    }

    public Column unaryAdd(Column x, T literal) {
        if (!checkUnaryValid(x, literal, '+') || x.size() <= 1) {
            throw new RuntimeException("ERROR: Either invalid operation for operands or empty column.");
        }
        String newColType = newType(x, literal);
        Column rv = new Column(x.findTitle(), newColType);
        for (int i = 1; i < x.size(); i += 1) {
            if (newColType.equals("string")) {
                rv.addItem(ao.strConcat((String) x.get(i), (String) literal));
            } else if (newColType.equals("int")) {
                rv.addItem(ao.add((Integer) x.get(i), (Integer) literal));
            }
            rv.addItem(ao.mAdd((Float) x.get(i), (Float) literal));
        }
        return rv;
    }

    public Column unaryMultiply(Column x, T literal) {
        if (!checkUnaryValid(x, literal, '*') || x.size() <= 1) {
            throw new RuntimeException("ERROR: Either invalid operation for operands or empty column.");
        }
        String newColType = newType(x, literal);
        Column rv = new Column(x.findTitle(), newColType);
        for (int i = 1; i < x.size(); i += 1) {
            if (isIntOp(x, literal)) {
                rv.addItem(ao.multiply((Integer) x.get(i), (Integer) literal));
            }
            rv.addItem(ao.mMultiply((Float) x.get(i), (Float) literal));
        }
        return rv;
    }

    public Column unaryDivide(Column x, T literal) {
        if (!checkUnaryValid(x, literal, '*') || x.size() <= 1) {
            throw new RuntimeException("ERROR: Either invalid operation for operands or empty column.");
        }
        String newColType = newType(x, literal);
        Column rv = new Column(x.findTitle(), newColType);
        for (int i = 1; i < x.size(); i += 1) {
            if (isIntOp(x, literal)) {
                rv.addItem(ao.divide((Integer) x.get(i), (Integer) literal));
            }
            rv.addItem(ao.mDivide((Float) x.get(i), (Float) literal));
        }
        return rv;
    }

    public Column unarySubtract(Column x, T literal) {
        if (!checkUnaryValid(x, literal, '*') || x.size() <= 1) {
            throw new RuntimeException("ERROR: Either invalid operation for operands or empty column.");
        }
        String newColType = newType(x, literal);
        Column rv = new Column(x.findTitle(), newColType);
        for (int i = 1; i < x.size(); i += 1) {
            if (isIntOp(x, literal)) {
                rv.addItem(ao.subtract((Integer) x.get(i), (Integer) literal));
            }
            rv.addItem(ao.mSubtract((Float) x.get(i), (Float) literal));
        }
        return rv;
    }


}
