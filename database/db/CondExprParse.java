package db;

/**
 * Created by Ethan Hu on 3/6/2017.
 */
public class CondExprParse {
    private StringCheck sc = new StringCheck();
    private String operand1;
    private String operand2;
    private String comparisonOp;

    public void parseConditional(String expr) {
        if (expr.equals(null) || expr.length() == 0) {
            throw new RuntimeException("Invalid conditional expression.");
        }
        String[] components = expr.split(" ");
        if (components.length != 3) {
            throw new RuntimeException("Invalid conditional expression, requires 2 operands and a comparison operator.");
        }
        this.operand1 = components[0];
        this.operand2 = components[2];
        this.comparisonOp = components[1];
    }

    public String getOperand1() {
        return this.operand1;
    }

    public String getOperand2() {
        return this.operand2;
    }

    public String getComparisonOp() {
        return this.comparisonOp;
    }

    public int findCompVal(String expr) {
        if (expr.equals(null) || expr.length() == 0) {
            throw new RuntimeException("Invalid comparison operator.");
        } else if (expr.equals("==")) {
            return 0;
        } else if (expr.equals(">=")) {
            return 1;
        } else if (expr.equals(">")) {
            return 2;
        } else if (expr.equals("<=")) {
            return -1;
        } else if (expr.equals("<")) {
            return -2;
        } else if (expr.equals("!=")) {
            return 10;
        } else {
            throw new RuntimeException("Invalid comparison operator.");
        }
    }

    public boolean checkValidComp(Table x, String op1, String op2) {
        if (x.equals(null) || x.findDepth() <= 1) {
            return false;
        } else if (x.findIndexName(op1) == -1) {
            return false;
        }
        Column first = x.getCol(x.findIndexName(op1));
        String fType = first.findType();
        if (x.findIndexName(op2) == -1) {
            if (sc.isNameString(op2)) {
                if (fType.equals("string")) {
                    return true;
                }
                return false;
            } else if (fType.equals("string")) {
                return false;
            }
            return true;
        }
        Column second = x.getCol(x.findIndexName(op2));
        String sType = second.findType();
        if (fType.equals("string") && sType.equals("string")) {
            return true;
        } else if (fType.equals("string") || sType.equals("string")) {
            return false;
        } else {
            return true;
        }
    }

    public String findCompType(Table x, String op1, String op2) {
        if (!checkValidComp(x, op1, op2)) {
            return "Invalid comparison.";
        }
        if (x.findIndexName(op2) == -1) {
            return "unary";
        } else {
            return "binary";
        }
    }

}
