package db;

import java.util.regex.*;
import java.awt.geom.FlatteningPathIterator;

/**
 * Created by Ethan Hu on 3/6/2017.
 */
public class ColExprParse {
    //Binary column expressions involve operations between 2 columns. Unary expressions involve operations between a column and a literal,
    //or just a column name for select.

    private String binOperand1;
    private String binOperand2;
    private String combinedName;

    private boolean isBinary;
    private Character aOperator;
    private boolean isSingleExpr;

    private String unaryOperand1;
    private String unaryOperand2;
    private UnaryColumnOps uco;
    private BinaryColumnOps bco = new BinaryColumnOps();
    private StringCheck sc = new StringCheck();

    //Takes in a Column Expression, parses it to operators & operands and identifies the operation type.
    public void parseColExpr(String expr) {
        if (expr.equals(null) || expr.length() == 0) {
            throw new RuntimeException("Invalid column expression.");
        }
        isBinary = false;
        isSingleExpr = false;
        Pattern operationExpr = Pattern.compile("([*]|[/]|[+]|[-])");
        Matcher checkIfOp = operationExpr.matcher(expr);
        //Checks for presence of arithmetic operator; if not present, then can only be single column select.
        if (checkIfOp.find()) {
            int opIndex = checkIfOp.start();
            aOperator = expr.charAt(opIndex);
            //Checks for 'as' keyword to indicate binary column operation; if not present, then has to be unary column operation.
            Pattern asExpr = Pattern.compile("((\\S+)\\s*([*]|[/]|[+]|[-])\\s*(\\S+))\\s+as\\s+(\\S+)");
            Matcher checkAsExpr = asExpr.matcher(expr);
            if (checkAsExpr.find()) {
                isBinary = true;
                String exprClause = checkAsExpr.group(1);
                combinedName = checkAsExpr.group(5).trim();
                String nwExpr = exprClause.replaceAll("\\s*", "");
                int newOpIndex = nwExpr.indexOf(aOperator);
                binOperand1 = nwExpr.substring(0, newOpIndex);
                binOperand2 = nwExpr.substring(newOpIndex + 1, nwExpr.length());
            } else {
                //Assume temporarily that it is a unary column op (can't check literal vs. col until supplied a table), throw Exceptions later if not.
                String nwExpr = expr.replaceAll("\\s+", "");
                int newOpIndex = nwExpr.indexOf(aOperator);
                unaryOperand1 = nwExpr.substring(0, newOpIndex);
                unaryOperand2 = nwExpr.substring(newOpIndex + 1, nwExpr.length());
            }
        } else {
            //Because there is no operator, this can only be the name of ONE column.
            isSingleExpr = true;
            String modExpr = expr.replaceAll("\\s+", "");
            unaryOperand1 = modExpr;
        }
    }


    //For testing purposes
    public String getbinOp1() {
        return this.binOperand1;
    }

    public String getbinOp2() {
        return this.binOperand2;
    }

    public String getCombinedName() {
        return this.combinedName;
    }

    public Character getaOperator() {
        return this.aOperator;
    }


    //Only use after calling parseColExpr, so that field variables will be set.
    public Column findOp(Table x) {
        if (isSingleExpr == true) {
            return selectCol(x, unaryOperand1);
        } else {
            if (isBinary == false) {
                if (x.findIndexName(unaryOperand2) != -1) {
                    if (x.findIndexName(unaryOperand1) == x.findIndexName(unaryOperand2)) {
                        return binaryOp(x, unaryOperand1, unaryOperand2, aOperator);
                    } else {
                        throw new RuntimeException("ERROR: Invalid binary column expression; second operand has to be literal " +
                                "or you will need to supply new combined Column name.");
                    }
                } else {
                    return unaryOp(x, unaryOperand1, unaryOperand2, aOperator);
                }
            } else {
                return binaryOp(x, binOperand1, binOperand2, aOperator);
            }
        }
    }


    //Returns column specified by colName in table.
    public Column selectCol(Table x, String colName) {
        if (x.findIndexName(colName) == -1) {
            throw new RuntimeException("ERROR: column not found in table.");
        }
        return x.getCol(x.findIndexName(colName));
    }


    public void unaryTypeSet(String literal) {
        if (sc.isNameString(literal)) {
            uco = new UnaryColumnOps<String>();
        } else if (sc.isFloatString(literal)) {
            uco = new UnaryColumnOps<Float>();
        } else if (sc.isIntString(literal)) {
            uco = new UnaryColumnOps<Integer>();
        }
    }


    public Column unaryOp(Table x, String colName, String literal, Character operator) {
        unaryTypeSet(literal);
        if (operator.equals('+')) {
            return uco.unaryAdd(x.getCol(x.findIndexTitle(colName)), literal);
        } else if (operator.equals('*')) {
            return uco.unaryMultiply(x.getCol(x.findIndexTitle(colName)), literal);
        } else if (operator.equals('/')) {
            return uco.unaryDivide(x.getCol(x.findIndexTitle(colName)), literal);
        } else if (operator.equals('-')) {
            return uco.unarySubtract(x.getCol(x.findIndexTitle(colName)), literal);
        } else {
            throw new RuntimeException("Undefined arithmetic operator.");
        }
    }

    //Type of new column might need to be inferred & appended to newTitle, come back to handle column join cases like these.
    public Column binaryOp(Table x, String colName1, String colName2, Character operator) {
        if (x.findIndexName(colName1) == -1 || x.findIndexName(colName2) == -1) {
            throw new RuntimeException("ERROR: at least one column isn't found in table.");
        }
        if (!combinedName.equals(null)) {
            int col1Index = x.findIndexName(colName1);
            int col2Index = x.findIndexName(colName2);
            if (operator.equals('+')) {
                return bco.binaryAdd(x.getCol(x.findIndexName(colName1)), x.getCol(x.findIndexName(colName2)), combinedName);
            } else if (operator.equals('*')) {
                return bco.binaryMultiply(x.getCol(x.findIndexName(colName1)), x.getCol(x.findIndexName(colName2)), combinedName);
            } else if (operator.equals('/')) {
                return bco.binaryDivide(x.getCol(x.findIndexName(colName1)), x.getCol(x.findIndexName(colName2)), combinedName);
            } else if (operator.equals('-')) {
                return bco.binarySubtract(x.getCol(x.findIndexName(colName1)), x.getCol(x.findIndexName(colName2)), combinedName);
            } else {
                throw new RuntimeException("Undefined arithmetic operator.");
            }
        } else {
            throw new RuntimeException("ERROR: Please provide new Column name after a binary column operation.");
        }
    }

    public static void main(String[] args) {
        String exprTest = "X       +          Y as whole";
        ColExprParse cepTest = new ColExprParse();
        cepTest.parseColExpr(exprTest);
    }
}
