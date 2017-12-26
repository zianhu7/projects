package db;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ethan Hu on 2/27/2017.
 */
public class Table<T extends Comparable<? super T>> {
    protected List<Column> columns;
    protected int colSize;
    protected int depth;
    private final StringCheck sc = new StringCheck();

    //never use, only in 3rd constructor to build table from predefined columns. Otherwise, will be empty table.
    public Table(int colSize, int depth) {
        this.columns = new ArrayList<>(colSize);
        this.colSize = colSize;
        this.depth = depth;
    }

    public Table(ArrayList<String> args) {
        colSize = args.size();
        columns = new ArrayList<>();
        for (int i = 0; i < colSize; i += 1) {
            String colHeader = args.get(i);
            String[] parts = colHeader.split(" ");
            columns.add(new Column(parts[0], parts[1]));
        }
        this.depth = 1;
    }

    public Table(List<Column> cols) {
        this(cols.size(), cols.get(0).size());
        this.columns = cols;
    }

    public Column getCol(int i) {
        return this.columns.get(i);
    }

    protected int findDepth() {
        return this.depth;
    }

    protected int findColSize() {
        return this.colSize;
    }

    protected String findColType(String name) {
        if (findIndexTitle(name) == -1) {
            return "ERROR: Column not found in table.";
        }
        Column tmp = getCol(findIndexTitle(name));
        return tmp.findType();
    }

    protected String findColTitle(int colindex) {
        Column tmp = this.getCol(colindex);
        return tmp.findTitle();
    }

    protected String findColName(int colindex) {
        Column tmp = this.getCol(colindex);
        return tmp.findName();
    }

    protected int findIndexTitle(String title) {
        for (int i = 0; i < this.colSize; i += 1) {
            String tmpTitle = this.findColTitle(i);
            if (tmpTitle.equals(title)) {
                return i;
            }
        }
        return -1;
    }

    protected int findIndexName(String name) {
        for (int i = 0; i < this.colSize; i += 1) {
            String tmpName = this.findColName(i);
            if (tmpName.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    //Works - returns appropriate arraylist of commonCols.
    protected ArrayList<String> commonCols(Table x, Table y) {
        if (x.equals(null) || y.equals(null)) {
            return null;
        }
        ArrayList<String> commonCols = new ArrayList<>();
        for (int i = 0; i < x.colSize; i += 1) {
            for (int j = 0; j < y.colSize; j += 1) {
                if (x.findColName(i).equals(y.findColName(j))) {
                    commonCols.add(x.findColTitle(i));
                }
            }
        }
        return commonCols;
    }


    protected ArrayList<String> titleConcatenate(Table x, Table y) {
        if (x.equals(null) || y.equals(null)) {
            throw new RuntimeException("Tables may be empty, please give me 2 well-defined tables.");
        }
        ArrayList titles = findTableTitles(x);
        titles.addAll(findTableTitles(y));
        return titles;
    }

    protected ArrayList<String> findTableTitles(Table x) {
        if (x.equals(null)) {
            return null;
        }
        ArrayList<String> titles = new ArrayList<>(x.colSize);
        for (int i = 0; i < x.colSize; i += 1) {
            titles.add(x.findColTitle(i));
        }
        return titles;
    }

    protected Table cartesianJoin(Table x, Table y) {
        if (x.equals(null) || y.equals(null)) {
            throw new RuntimeException("Can not join empty tables, please check Tables passed in.");
        }
        ArrayList<String> concatTitles = titleConcatenate(x, y);
        Table z = new Table(concatTitles);
        for (int i = 1; i < x.depth; i += 1) {
            for (int j = 1; j < y.depth; j += 1) {
                Row tmp = x.getRow(i);
                Row merged = tmp.mergeRows(tmp, y.getRow(j));
                z.addRow(merged);
            }
        }
        return z;
    }


    //checkOverlap checks x's row at xdepth vs. y's row at ydepth and
    // returns true if both rows have same values for ALL shared columns in commonCols, false otherwise.
    //Further: RowIndex denotes row that columns.get would retrieve, not depth index.
    protected boolean checkOverlap(Table x, int xRIndex, Table y, int yRIndex, ArrayList<String> commonCols) {
        if (!checkIndices(x, xRIndex)) {
            throw new RuntimeException("xRowIndex is not valid - either title row or out of bounds.");
        } else if (!checkIndices(y, yRIndex)) {
            throw new RuntimeException("yRowIndex is not valid - either title row or out of bounds.");
        }
        for (int i = 0; i < commonCols.size(); i += 1) {
            String tmpTitle = commonCols.get(i);
            Column xTmp = x.getCol(x.findIndexTitle(tmpTitle));
            Column yTmp = y.getCol(y.findIndexTitle(tmpTitle));
            if (!xTmp.get(xRIndex).equals(yTmp.get(yRIndex))) {
                return false;
            }
        }
        return true;
    }

    //Checks if indices passed in for Rows of tables are valid.
    protected boolean checkIndices(Table x, int xIndex) {
        if (xIndex >= x.depth) {
            return false;
        } else if (xIndex <= 0) {
            return false;
        }
        return true;
    }

    //Returns array of titles in the order required by join (not Cartesian join).
    protected ArrayList<String> jColOrder(Table x, Table y, ArrayList<String> commonCols) {
        if (x.equals(null) || y.equals(null)) {
            throw new RuntimeException("Can't join with empty tables, please check table input.");
        }
        ArrayList<String> xTitles = findTableTitles(x);
        ArrayList<String> yTitles = findTableTitles(y);
        ArrayList<String> joinedTitles = new ArrayList<>();
        for (int i = 0; i < commonCols.size(); i += 1) {
            String title = commonCols.get(i);
            joinedTitles.add(title);
            xTitles.remove(title);
            yTitles.remove(title);
        }
        joinedTitles.addAll(xTitles);
        joinedTitles.addAll(yTitles);
        return joinedTitles;
    }

    //Handles row merge in Join; rearranges rows based on Join order, takes in joinedTitles as basis and returns merged Row.
    //As above, use RowIndices instead of depth.
    protected Row mergeJRows(Table x, int xRIndex, Table y, int yRIndex, ArrayList<String> joinedTitles) {
        if (!checkIndices(x, xRIndex)) {
            throw new RuntimeException("xRowIndex is not valid - either title row or out of bounds.");
        } else if (!checkIndices(y, yRIndex)) {
            throw new RuntimeException("yRowIndex is not valid - either title row or out of bounds.");
        }
        Row mergedRow = new Row();
        for (int i = 0; i < x.colSize; i += 1) {
            Column xTmp = x.getCol(findIndexTitle(joinedTitles.get(i)));
            mergedRow.addItem(xTmp.get(xRIndex));
        }
        for (int j = x.colSize; j < joinedTitles.size(); j += 1) {
            String coltitle = joinedTitles.get(j);
            Column yTmp = y.getCol(y.findIndexTitle(coltitle));
            mergedRow.addItem(yTmp.get(yRIndex));
        }
        return mergedRow;
    }

    public Table join(Table x, Table y) {
        ArrayList<String> cc = commonCols(x, y);
        if (cc.size() == 0) {
            Table rv = cartesianJoin(x, y);
            return rv;
        }
        ArrayList<String> jCO = jColOrder(x, y, cc);
        Table rvJoin = new Table(jCO);
        for (int i = 1; i < x.depth; i += 1) {
            for (int j = 1; j < y.depth; j += 1) {
                if (checkOverlap(x, i, y, j, cc)) {
                    Row tmp = mergeJRows(x, i, y, j, jCO);
                    rvJoin.addRow(tmp);
                }
            }
        }
        return rvJoin;
    }

    public Table mJoin(ArrayList<Table> tables) {
        if (tables.size() == 0) {
            throw new RuntimeException("ERROR: Empty collection of tables.");
        } else if (tables.size() == 1) {
            return tables.get(0);
        } else if (tables.size() == 2) {
            return join(tables.get(0), tables.get(1));
        }
        Table tmp = join(tables.get(0), tables.get(1));
        for (int i = 2; i < tables.size(); i += 1) {
            if (tables.get(i).equals(null)) {
                throw new RuntimeException("ERROR: Can't join a null table.");
            }
            tmp = join(tmp, tables.get(i));
        }
        return tmp;
    }


    protected void addRow(Row x) {
        if (x.getLength() != this.colSize) {
            throw new RuntimeException("Please make sure Row length is same as # of Columns.");
        }
        for (int i = 0; i < this.colSize; i += 1) { //Also when doing final imp make sure to check type of each val
            Column tmp = this.getCol(i);
            String val = x.get(i).toString();
            if (sc.isNameString(val) && !tmp.findType().equals("string")) {
                throw new RuntimeException("ERROR: Value " + val + " does not match the type of the column.");
            } else if (sc.isFloatString(val) && !tmp.findType().equals("float")) {
                throw new RuntimeException("ERROR: Value " + val + " does not match the type of the column.");
            } else if (sc.isIntString(val) && !tmp.findType().equals("int")) {
                throw new RuntimeException("ERROR: Value " + val + " does not match the type of the column.");
            } else {
                tmp.addItem(x.get(i));
            }
        }
        this.depth += 1;
    }

    //Gets the row of the table based on ArrayList columns indexing, not depth.
    public Row getRow(int index) {
        if (index >= this.depth) {
            throw new IndexOutOfBoundsException("Index is beyond depth of the table.");
        }
        Row rv = new Row(this.colSize);
        for (int i = 0; i < this.colSize; i += 1) {
            rv.addItem(this.getCol(i).get(index));
        }
        return rv;
    }

    public String printTable() {
        StringBuilder sb = new StringBuilder();
        if (this.colSize == 0 ) {
            return "ERROR: This is an empty table you are trying to print.";
        }
        Row colTitles = this.getRow(0);
        if (this.depth == 1) {
            sb.append(colTitles.convertRow());
            return sb.toString();
        }
        sb.append(colTitles.convertRow() + "\n");
        for (int i = 1; i < this.depth - 1; i += 1) {
            Row tmp = this.getRow(i);
            sb.append(tmp.sbConcatRow() + "\n");
        }
        sb.append(this.getRow(depth - 1).sbConcatRow());
        return sb.toString();
    }

    public static void main(String[] args) {
            ArrayList<String> titles = new ArrayList<>();
            titles.add("X int");
            titles.add("Y int");
            Table testTable = new Table(titles);

            ArrayList<Integer> r1 = new ArrayList<>();
            r1.add(2);
            r1.add(5);
            Row row1 = new Row(r1);
            ArrayList<Integer> r2 = new ArrayList<>();
            r2.add(4);
            r2.add(6);
            Row row2 = new Row(r2);
            testTable.addRow(row1);
            testTable.addRow(row2);
            ArrayList<String> titles2 = new ArrayList<>();
            titles2.add("X int");
            titles2.add("Y2 int");
            Table testTable2 = new Table(titles2);

            ArrayList<Integer> r3 = new ArrayList<>();
            r3.add(2);
            r3.add(7);
            Row row3 = new Row(r3);
            ArrayList<Integer> r4 = new ArrayList<>();
            r4.add(4);
            r4.add(6);
            Row row4 = new Row(r4);
            testTable2.addRow(row3);
            testTable2.addRow(row4);
            Table rv = testTable.join(testTable, testTable2);

    }
}




        /*
        colTitles.add("X int");
        colTitles.add("Y string");
        Table rv = new Table(colTitles);
        Row x = new Row(2);
        x.addItem(2);
        x.addItem("aef");
        Row y = new Row(3);
        y.addItem(5);
        y.addItem("wet");
        rv.addRow(x);
        rv.addRow(y);
        ArrayList<String> colTitles2 = new ArrayList<>(2);
        colTitles2.add("X2 int");
        colTitles2.add("Y2 string");
        Table rv2 = new Table(colTitles2);
        Row x2 = new Row(2);
        x2.addItem(51);
        x2.addItem("klm");
        Row y2 = new Row(3);
        y2.addItem(10);
        y2.addItem("dry");
        rv2.addRow(x);
        rv2.addRow(y);
        rv.join(rv,rv2).printTable();


Table newTable = new Table(colTitles);
        Integer[] r1 = new Integer[]{1, 2, 3};
        Row row1 = new Row(r1);
        Integer[] r2 = new Integer[]{2,3,4};
        Row row2 = new Row(r2);
        Integer[] r3 = new Integer[]{3,4,5};
        Row row3 = new Row(r3);
        newTable.addRow(row1);
        newTable.addRow(row2);
        newTable.addRow(row3);

        ArrayList<String> colTitles2 = new ArrayList();
        colTitles2.add("A int");
        colTitles2.add("B int");
        colTitles2.add("C int");

        Table newTable2 = new Table(colTitles2);
        Integer[] r1a = new Integer[]{3,2,7};
        Row row1a = new Row(r1a);
        Integer[] r2a = new Integer[]{6,5,8};
        Row row2a = new Row(r2a);
        Integer[] r3a = new Integer[]{4,3,1};
        Row row3a = new Row(r3a);
        newTable2.addRow(row1a);
        newTable2.addRow(row2a);
        newTable2.addRow(row3a);

        ArrayList<String> commonCols = newTable.commonCols(newTable, newTable2);
        ArrayList<String> newArray = newTable.jColOrder(newTable, newTable2, commonCols);
        Table tmp = newTable.join(newTable, newTable2);
        tmp.printTable();
}*/

