package db;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ethan Hu on 3/3/2017.
 */
public class SelectOperator<T extends Comparable<? super T>> {

    public Table unarySelect(Table x, String colTitle, T value, UnaryFilter y, int compval) {
        if (x.equals(null) || x.findDepth() == 1) {
            throw new RuntimeException("Please provide a well-defined table; either null or no rows.");
        }
        Column tmp = x.getCol(x.findIndexName(colTitle));
        ArrayList<Integer> rowIndices = y.findFilter(tmp, value, compval); //Come back to check raw type cast issues, decide whether modifications are necessary.
        Table filteredRV = new Table(x.findTableTitles(x));
        for (int i = 0; i < rowIndices.size(); i += 1) {
            Row p = x.getRow(rowIndices.get(i));
            filteredRV.addRow(p); //Good example of less confusing, non-repetitive non-static method calls - no need for table params, use "this". Edit Table, etc.
        }
        return filteredRV;
    }

    public Table binarySelect(Table x, String colTitleA, String colTitleB, BinaryFilter y, int compval) {
        if (x.equals(null) || x.findDepth() == 1) {
            throw new RuntimeException("Please provide a well-defined table; either null or no rows.");
        }
        Column tmpA = x.getCol(x.findIndexName(colTitleA));
        Column tmpB = x.getCol(x.findIndexName(colTitleB));
        ArrayList<Integer> rowIndices = y.findFilter(tmpA, tmpB, compval); //Come back to check raw type cast issues, decide whether modifications are necessary.
        Table filteredRV = new Table(x.findTableTitles(x));
        for (int i = 0; i < rowIndices.size(); i += 1) {
            Row p = x.getRow(rowIndices.get(i));
            filteredRV.addRow(p); //Good example of less confusing, non-repetitive non-static method calls - no need for table params, use "this". Edit Table, etc.
        }
        return filteredRV;
    }

    /*public static void main(String[] args) {
        ArrayList<String> colTitles = new ArrayList<>();
        colTitles.add("X int");
        colTitles.add("Y int");
        colTitles.add("Z int");

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

        /*ArrayList<String> colTitles2 = new ArrayList();
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
        SelectOperator so = new SelectOperator();
        Table rv = so.binarySelect(newTable, "X int", "Y int", new BinaryFilter(), 2);
        rv.printTable();

    }*/
}
