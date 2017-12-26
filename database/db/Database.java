package db;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class Database {
    private HashMap<String, Table> datamap;
    private final DbParser dbparser = new DbParser();
    private final StringCheck sc = new StringCheck();
    private final CondExprParse condparser = new CondExprParse();
    private final TypeChecker tc = new TypeChecker();
    private final SelectOperator<String> soString = new SelectOperator<>();
    private final SelectOperator<Float> soFloat = new SelectOperator<>();
    private final ColExprParse colparser = new ColExprParse();
    private final TableReader tr = new TableReader();
    private final TableWriter tw = new TableWriter();
    // Various common constructs, simplifies parsing.
    private static final String REST = "\\s*(.*)\\s*",
            COMMA = "\\s*,\\s*",
            AND = "\\s+and\\s+";

    // Stage 1 syntax, contains the command name.
    private static final Pattern CREATE_CMD = Pattern.compile("create table " + REST),
            LOAD_CMD = Pattern.compile("load " + REST),
            STORE_CMD = Pattern.compile("store " + REST),
            DROP_CMD = Pattern.compile("drop table " + REST),
            INSERT_CMD = Pattern.compile("insert into " + REST),
            PRINT_CMD = Pattern.compile("print " + REST),
            SELECT_CMD = Pattern.compile("select " + REST);

    // Stage 2 syntax, contains the clauses of commands.
    private static final Pattern CREATE_NEW = Pattern.compile("(\\S+)\\s+\\((\\S+\\s+\\S+\\s*"
            + "(?:,\\s*\\S+\\s+\\S+\\s*)*)\\)"),
            SELECT_CLS = Pattern.compile("([^,]+?(?:,[^,]+?)*)\\s+from\\s+"
                    + "(\\S+\\s*(?:,\\s*\\S+\\s*)*)(?:\\s+where\\s+"
                    + "([\\w\\s+\\-*/'<>=!.]+?(?:\\s+and\\s+"
                    + "[\\w\\s+\\-*/'<>=!.]+?)*))?"),
            CREATE_SEL = Pattern.compile("(\\S+)\\s+as select\\s+"
                    + SELECT_CLS.pattern()),
            INSERT_CLS = Pattern.compile("(\\S+)\\s+values\\s+(.+?"
                    + "\\s*(?:,\\s*.+?\\s*)*)");

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Database database = (Database) o;

        if (datamap != null ? !datamap.equals(database.datamap)
                : database.datamap != null) {
            return false;
        }
        if (dbparser != null ? !dbparser.equals(database.dbparser)
                : database.dbparser != null) {
            return false;
        }
        if (sc != null ? !sc.equals(database.sc) : database.sc != null) {
            return false;
        }
        if (condparser != null ? !condparser.equals(database.condparser)
                : database.condparser != null) {
            return false;
        }
        if (tc != null ? !tc.equals(database.tc) : database.tc != null) {
            return false;
        }
        if (soString != null ? !soString.equals(database.soString)
                : database.soString != null) {
            return false;
        }
        if (soFloat != null ? !soFloat.equals(database.soFloat)
                : database.soFloat != null) {
            return false;
        }
        return colparser != null ? colparser.equals(database.colparser)
                : database.colparser == null;
    }

    @Override
    public int hashCode() {
        int result = datamap != null ? datamap.hashCode() : 0;
        result = 31 * result + (dbparser != null ? dbparser.hashCode() : 0);
        result = 31 * result + (sc != null ? sc.hashCode() : 0);
        result = 31 * result + (condparser != null ? condparser.hashCode() : 0);
        result = 31 * result + (tc != null ? tc.hashCode() : 0);
        result = 31 * result + (soString != null ? soString.hashCode() : 0);
        result = 31 * result + (soFloat != null ? soFloat.hashCode() : 0);
        result = 31 * result + (colparser != null ? colparser.hashCode() : 0);
        return result;
    }

    private class DbParser {

        private String eval(String query) throws IOException {
            Matcher m;
            if ((m = CREATE_CMD.matcher(query)).matches()) {
                String tmp = trim(m.group(1));
                return createTable(tmp);
            } else if ((m = LOAD_CMD.matcher(query)).matches()) {
                String tmp = trim(m.group(1));
                return loadTable(tmp);
            } else if ((m = STORE_CMD.matcher(query)).matches()) {
                String tmp = trim(m.group(1));
                return storeTable(tmp);
            } else if ((m = DROP_CMD.matcher(query)).matches()) {
                String tmp = trim(m.group(1));
                return dropTable(tmp);
            } else if ((m = INSERT_CMD.matcher(query)).matches()) {
                String tmp = trim(m.group(1));
                return insertRow(tmp);
            } else if ((m = PRINT_CMD.matcher(query)).matches()) {
                String tmp = trim(m.group(1));
                return printTable(tmp);
            } else if ((m = SELECT_CMD.matcher(query)).matches()) {
                String tmp = trim(m.group(1));
                return select(tmp);
            } else {
                return "ERROR: Malformed query: " + query;
            }
        }

        //Trying to store the table named tableName --> retrieves table value at that key
        // in hashmap and stores to <tableName>.tbl at path specified at main call.
        private String storeTable(String tableName) {
            return dbStore(tableName);
        }

        private String loadTable(String tableName) {
            return dbLoad(tableName);
        }

        private String printTable(String name) {
            return dbPrint(name);
        }

        private String dropTable(String expr) {
            return dbDrop(expr);
        }

        private Row typeConvert(String[] values) {
            if (values.equals(null) || values.length == 0) {
                return null;
            }
            Row rv = new Row(values.length);
            for (int i = 0; i < values.length; i += 1) {
                if (sc.isIntString(values[i])) {
                    rv.addItem(Integer.valueOf(values[i]));
                } else if (sc.isFloatString(values[i])) {
                    rv.addItem(Float.valueOf(values[i]));
                } else if (sc.isNameString(values[i])) {
                    rv.addItem(values[i]);
                } else {
                    throw new RuntimeException("ERROR: You tried to enter a "
                            + "data type that is not allowed.");
                }
            }
            return rv;
        }


        private String insertRow(String expr) {
            expr = trim(expr);
            Matcher m = INSERT_CLS.matcher(expr);
            if (!m.matches()) {
                return "ERROR: Malformed insert: " + expr;
            }
            String[] values = m.group(2).split(COMMA);
            try {
                Row rv = typeConvert(values);
                return dbInsertInto(m.group(1), rv);
            } catch (RuntimeException e) {
                return e.getMessage();
            }
        }

        private String select(String expr) {
            expr = trim(expr);
            Matcher m = SELECT_CLS.matcher(expr);
            if (!m.matches()) {
                return "ERROR: Malformed select: " + expr;
            }
            String[] tableExprs = m.group(2).split(COMMA);
            String[] colExprs = m.group(1).split(COMMA);

            try {
                String[] condExprs = m.group(3).split(AND);
                return dbSelect(colExprs, tableExprs, condExprs, false, "");
            } catch (NullPointerException e) {
                return dbUnarySelect(colExprs, tableExprs, false, "");
            }
            //Make sure to implement print table well, then call dbPrint on
            // result of dbSelect to print table.
        }

        //Parse an expression clause based on comma delimiters - ONLY use for
        // comma-dependent clauses.
        private String[] parseExpr(String expr) {
            expr = trim(expr);
            if (expr.equals(null)) {
                return null;
            }
            return expr.split(COMMA);
        }


        private String createTable(String expr) {
            expr = trim(expr);
            Matcher m;
            if ((m = CREATE_NEW.matcher(expr)).matches()) {
                return createNewTable(m.group(1), m.group(2).split(COMMA));
            } else if ((m = CREATE_SEL.matcher(expr)).matches()) {
                return createSelectedTable(m.group(1), m.group(2),
                        m.group(3), m.group(4));
            } else {
                return "ERROR: Malformed create: " + expr;
            }
        }

        private String createSelectedTable(String name, String exprs,
                                           String tables, String conds) {
            name = trim(name);
            exprs = trim(exprs);
            tables = trim(tables);
            String[] sepExpr = exprs.split(COMMA);
            String[] sepTables = tables.split(COMMA);
            if (conds != null) {
                String[] sepConds = conds.split(AND);
                dbSelect(sepExpr, sepTables, sepConds, true, name);
                return "";
            } else {
                dbUnarySelect(sepExpr, sepTables, true, name);
                return "";
            }
        }

        private String createNewTable(String name, String[] cols) {
            name = trim(name);
            ArrayList<String> colNames = new ArrayList<>(Arrays.asList(cols));
            try {
                Table rv = new Table(colNames);
                return dbCreate(name, rv);
            } catch (RuntimeException e) {
                return e.getMessage();
            }
        }
    }

    public Database() {
        datamap = new HashMap<>();
    }

    //Handles select for when there are no conditional expressions, ASSUMES expressions &
    // tables aren't null (because of regex to String[] parsing in parse).
    private String dbUnarySelect(String[] exprs, String[] tables,
                                 boolean storeSelect, String newHeader) {
        newHeader = trim(newHeader);
        Table joinedTable;
        try {
            joinedTable = dbJoin(tables);
        } catch (RuntimeException e) {
            return e.getMessage();
        }
        List<Column> selectedCols = new ArrayList<>();
        for (int i = 0; i < exprs.length; i += 1) {
            exprs[i] = trim(exprs[i]);
            Character allCheck = exprs[0].charAt(0);
            if (allCheck.equals('*')) {
                if (storeSelect && !newHeader.equals("")) {
                    datamap.put(newHeader, joinedTable);
                    return joinedTable.printTable();
                }
                return joinedTable.printTable();
            }
            try {
                colparser.parseColExpr(exprs[i]);
                Column tmp = colparser.findOp(joinedTable);
                selectedCols.add(tmp);
            } catch (RuntimeException e) {
                return e.getMessage();
            }
        }
        Table rv = new Table(selectedCols);
        if (storeSelect && !newHeader.equals("")) {
            datamap.put(newHeader, rv);
            return rv.printTable();
        } else {
            return rv.printTable();
        }
    }

    //Tentative first draft of Select, returns table resulting from selecting Column
    // exprs from joined table (made from tables) and filtered beforehand by conds.
    private String dbSelect(String[] exprs, String[] tables, String[] conds,
                            boolean storeSelect, String newHeader) {
        newHeader = trim(newHeader);
        Table joinedTable;
        try {
            joinedTable = dbJoin(tables);
        } catch (RuntimeException e) {
            return e.getMessage();
        }
        try {
            for (int i = 0; i < conds.length; i += 1) {
                conds[i] = trim(conds[i]);
                condparser.parseConditional(conds[i]);
                String op1 = condparser.getOperand1();
                String op2 = condparser.getOperand2();
                int compVal = condparser.findCompVal(condparser.getComparisonOp());
                String compType = condparser.findCompType(joinedTable, op1, op2);
                if (compType.equals("unary")) {
                    if (sc.isNameString(op2)) {
                        joinedTable = soString.unarySelect(joinedTable, op1, op2,
                                new UnaryFilter(), compVal);
                    } else {
                        joinedTable = soFloat.unarySelect(joinedTable, op1,
                                Float.valueOf(op2), new UnaryFilter(), compVal);
                    }
                } else if (compType.equals("binary")) {
                    if (joinedTable.findColType(op1).equals("string")
                            && joinedTable.findColType(op2).equals("string")) {
                        joinedTable = soString.binarySelect(joinedTable, op1, op2,
                                new BinaryFilter(), compVal);
                    }
                    joinedTable = soFloat.binarySelect(joinedTable, op1,
                            op2, new BinaryFilter(), compVal);
                }
            }
            List<Column> filteredCols = new ArrayList<>();
            for (int j = 0; j < exprs.length; j += 1) {
                exprs[j] = trim(exprs[j]);
                Character allCheck = exprs[0].charAt(0);
                if (allCheck.equals('*')) {
                    if (storeSelect && !newHeader.equals("")) {
                        datamap.put(newHeader, joinedTable);
                        return joinedTable.printTable();
                    }
                    return joinedTable.printTable();
                }
                colparser.parseColExpr(exprs[j]);
                Column tmp = colparser.findOp(joinedTable);
                filteredCols.add(tmp);
            }
            Table rv = new Table(filteredCols);
            if (storeSelect && !newHeader.equals("")) {
                datamap.put(newHeader, rv);
                return rv.printTable();
            } else {
                return rv.printTable();
            }
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    private String dbLoad(String tableName) {
        tableName = trim(tableName);
        String fileName = tableName + ".tbl";
        String[] tableRows;
        try {
            tableRows = tr.tableReader(fileName);
        } catch (IOException e) {
            return "ERROR: " + e.getMessage();
        }
        if (tableRows[0] == null) {
            return "ERROR: Not supposed to load emtpy table.";
        }
        String[] titles = tableRows[0].split(",");
        ArrayList<String> titlesArray = new ArrayList<>(Arrays.asList(titles));
        Table loaded;
        try {
            loaded = new Table(titlesArray);
        } catch (RuntimeException e) {
            return "ERROR: " + e.getMessage();
        }
        for (int i = 1; i < tableRows.length; i += 1) {
            String[] rowVals = tableRows[i].split(",");
            Row tmp = new Row();
            for (int j = 0; j < rowVals.length; j += 1) {
                rowVals[j] = trim(rowVals[j]);
                if (sc.isNameString(rowVals[j])) {
                    tmp.addItem(rowVals[j]);
                } else if (sc.isFloatString(rowVals[j])) {
                    tmp.addItem(Float.valueOf(rowVals[j]));
                } else if (sc.isIntString(rowVals[j])) {
                    tmp.addItem(Integer.valueOf(rowVals[j]));
                } else {
                    return "ERROR: trying to load table with invalid types.";
                }
            }
            try {
                loaded.addRow(tmp);
            } catch (RuntimeException e) {
                return "ERROR: " + e.getMessage();
            }
        }
        return dbCreate(tableName, loaded);
    }

    private String dbStore(String tableName) {
        tableName = trim(tableName);
        String fileName = tableName + ".tbl";
        if (!datamap.containsKey(tableName)) {
            return "ERROR: This database does not contain specified table.";
        }
        Table tmp = datamap.get(tableName);
        String tableContent = tmp.printTable();
        try {
            FileWriter fw = new FileWriter(fileName);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(tableContent);
            bw.close();
        } catch (IOException e) {
            return e.getMessage();
        }
        return "";
    }

    private Table dbJoin(String[] tables) {
        ArrayList<Table> tableCollection = new ArrayList<>();
        for (int i = 0; i < tables.length; i += 1) {
            if (!datamap.containsKey(tables[i])) {
                throw new RuntimeException("ERROR: Database doesn't contain the table.");
            }
            tableCollection.add(datamap.get(tables[i]));
        }
        Table tmp = tableCollection.get(0);
        if (tableCollection.size() == 1) {
            return tmp;
        }
        Table rv = tmp.mJoin(tableCollection);
        return rv;
    }


    private String dbPrint(String tableheader) {
        tableheader = trim(tableheader);
        if (!this.datamap.containsKey(tableheader)) {
            return "ERROR: This database doesn't contain the "
                    + "specified table, please check table name.";
        }
        Table tmp = datamap.get(tableheader);
        return tmp.printTable();
    }

    private String dbCreate(String tableheader, Table x) {
        tableheader = trim(tableheader);
        if (this.datamap.containsValue(x)) {
            return "ERROR: Should not create a table that "
                    + "already exists - please pass in a new table.";
        }
        datamap.put(tableheader, x);
        return "";
    }

    private String dbInsertInto(String tableheader, Row x) {
        tableheader = trim(tableheader);
        if (!datamap.containsKey(tableheader)) {
            return "ERROR: This database doesn't contain the "
                    + "specified table, please check table name.";
        } else if (x.getLength() != datamap.get(tableheader).findColSize()) {
            return "ERROR: Malformed insertion, row length "
                    + "does not match number of columns in table.";
        } else {
            Table tmp = datamap.get(tableheader);
            try {
                tmp.addRow(x);
                return "";
            } catch (RuntimeException e) {
                return e.getMessage();
            }
        }
    }

    private String trim(String x) {
        x = x.replaceAll("\\s+", " ");
        return x.replaceAll("\\s+$", "");
    }

    private String dbDrop(String tableheader) {
        tableheader = trim(tableheader);
        if (!datamap.containsKey(tableheader)) {
            return "ERROR: This database does not have Table " + tableheader
                    + " stored. Pass in a valid table key.";
        }
        datamap.remove(tableheader);
        return "";
    }


    public String transact(String query) {
        try {
            query = query.replaceAll("\\s+", " ");
            return dbparser.eval(query);
        } catch (IOException e) {
            return "ERROR: " + e.getMessage();
        } catch (RuntimeException f) {
            return "ERROR: " + f.getMessage();
        }
    }

    /*public static void main(String[] args) {
        Database x = new Database();
        x.transact("load Ntest");
        x.transact("load Atest");
        x.transact("load temp");
        x.transact("select * from Ntest,Atest");
    }*/
}
