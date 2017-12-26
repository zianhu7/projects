package db;

/**
 * Created by Ethan Hu on 3/4/2017.
 */
public class BinaryColumnOps<T> {
    private final TypeChecker tc = new TypeChecker();
    private final ArithmeticOps ao = new ArithmeticOps();

    public boolean checkBinaryValid(Column x, Column y, String operator) {
        String xType = x.findType();
        String yType = y.findType();
        if (isStrOp(x, y)) {
            if (operator.equals("+")) {
                return true;
            } else {
                throw new RuntimeException("ERROR: Only addition is defined for operations on string-type columns.");
            }
        }
        return true;
    }

    public boolean isIntOp(Column x, Column y) {
        if (x.findType().equals("int") && y.findType().equals("int")) {
            return true;
        }
        return false;
    }

    public boolean isFloatOp(Column x, Column y) {
        if (x.findType().equals("int") && y.findType().equals("float")) {
            return true;
        } else if (x.findType().equals("float") && y.findType().equals("int")) {
            return true;
        } else if (x.findType().equals("float") && y.findType().equals("float")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isStrOp(Column x, Column y) {
        if (x.findType().equals("string") && y.findType().equals("string")) {
            return true;
        }
        return false;
    }

    public String newType(Column x, Column y) {
        if (isIntOp(x, y)) {
            return "int";
        } else if (isStrOp(x, y)) {
            return "string";
        } else if (isFloatOp(x, y)) {
            return "float";
        } else {
            throw new RuntimeException("ERROR: Can't find the new type of combined columns from x & y, not defined.");
        }
    }

    public Column binaryAdd(Column x, Column y, String newTitle) {
        if (!checkBinaryValid(x, y, "+") || x.size() <= 1 || x.size() != y.size()) {
            throw new RuntimeException("ERROR: Either invalid operation on columns or columns are empty or not the same size.");
        }
        String combinedType = newType(x, y);
        Column rv = new Column(newTitle, combinedType);
        for (int i = 1; i < x.size(); i += 1) {
            String val1 = x.get(i).toString();
            String val2 = y.get(i).toString();
            if (combinedType.equals("int")) {
                rv.addItem(ao.add(Integer.valueOf(val1), Integer.valueOf(val2)));
            } else if (combinedType.equals("string")) {
                rv.addItem(ao.strConcat(val1, val2));
            } else if (combinedType.equals("float")) {
                rv.addItem(ao.mAdd(Float.valueOf(val1), Float.valueOf(val2)));
            } else {
                throw new RuntimeException("ERROR: Invalid operation, undefined for this combination of data types.");
            }
        }
        return rv;
    }

    public Column binaryMultiply(Column x, Column y, String newTitle) {
        if (!checkBinaryValid(x, y, "*") || x.size() <= 1 || x.size() != y.size()) {
            throw new RuntimeException("ERROR: Either invalid operation on columns or columns are empty or not the same size.");
        }
        String combinedType = newType(x, y);
        Column rv = new Column(newTitle, combinedType);
        for (int i = 1; i < x.size(); i += 1) {
            if (combinedType.equals("int")) {
                rv.addItem(ao.multiply((Integer) x.get(i), (Integer) y.get(i)));
            }
            rv.addItem(ao.mMultiply((Float) x.get(i), (Float) y.get(i)));
        }
        return rv;
    }

    public Column binaryDivide(Column x, Column y, String newTitle) {
        if (!checkBinaryValid(x, y, "/") || x.size() <= 1 || x.size() != y.size()) {
            throw new RuntimeException("ERROR: Either invalid operation on columns or columns are empty or not the same size.");
        }
        String combinedType = newType(x, y);
        Column rv = new Column(newTitle, combinedType);
        for (int i = 1; i < x.size(); i += 1) {
            if (combinedType.equals("int")) {
                rv.addItem(ao.divide((Integer) x.get(i), (Integer) y.get(i)));
            }
            rv.addItem(ao.mDivide((Float) x.get(i), (Float) y.get(i)));
        }
        return rv;
    }

    public Column binarySubtract(Column x, Column y, String newTitle) {
        if (!checkBinaryValid(x, y, "-") || x.size() <= 1 || x.size() != y.size()) {
            throw new RuntimeException("ERROR: Either invalid operation on columns or columns are empty or not the same size.");
        }
        String combinedType = newType(x, y);
        Column rv = new Column(newTitle, combinedType);
        for (int i = 1; i < x.size(); i += 1) {
            if (combinedType.equals("int")) {
                rv.addItem(ao.subtract((Integer) x.get(i), (Integer) y.get(i)));
            }
            rv.addItem(ao.mSubtract((Float) x.get(i), (Float) y.get(i)));
        }
        return rv;
    }
}
