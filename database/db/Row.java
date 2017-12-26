package db;

import org.junit.Test;

import java.util.*;

/**
 * Created by Ethan Hu on 2/27/2017.
 */
public class Row<T extends Comparable<? super T>> {
    private ArrayList<T> row;
    private int length;
    private final StringCheck sc = new StringCheck();
    private final TypeChecker tc = new TypeChecker();

    //Creates empty row, check that this is NOT used especially when merging/attaching to tables
    public Row() {
        length = 0;
        row = new ArrayList<>();
    }

    //Creates an empty Row of initial capacity length
    public Row(int length) {
        this.length = 0;
        row = new ArrayList<T>(length);
    }

    public Row(T[] values) {
        this.length = values.length;
        row = new ArrayList<T>(this.length);
        for (int i = 0; i < values.length; i += 1) {
            row.add(values[i]);
        }
    }

    //Creates a Row with items in values
    public Row(ArrayList<T> values) {
        this.length = values.size();
        row = new ArrayList<T>(length);
        for (int i = 0; i < values.size(); i += 1) {
            row.add(values.get(i));
        }
    }

    public Row mergeRows(Row x, Row y) {
        Row rv = new Row();
        for (int i = 0; i < x.getLength(); i += 1) {
            rv.addItem((T) x.get(i));
        }
        for (int j = 0; j < y.getLength(); j += 1) {
            rv.addItem((T) y.get(j));
        }
        return rv;
    }

    public String convertRow() {
        if (this.getLength() == 0) {
            return "";
        }
        String tmp = "";
        for (int i = 0; i < length - 1; i += 1) {
            tmp += get(i).toString() + ",";
        }
        tmp += this.get(length - 1).toString();
        return tmp;
    }

    public String sbConcatRow() {
        StringBuilder sb = new StringBuilder();
        if (this.equals(null)) {
            throw new RuntimeException("ERROR: empty row, can't print.");
        }
        for (int i = 0; i < this.length - 1; i += 1) {
            String tmp = this.get(i).toString();
            if (sc.isNameString(tmp)) {
                sb.append(tmp);
                sb.append(',');
            //Formatting leading zeros, stackoverflow post source: http://stackoverflow.com/a/2800839
            } else if (sc.isIntString(tmp)) {
                String x = this.get(i).toString();
                x = x.replaceFirst("^0+(?!$)", "");
                sb.append(tmp);
                sb.append(',');
            //Formatting float decimal to 3 places
            } else if (sc.isFloatString(tmp)) {
                String formatTmp = String.format("%.3f", Float.valueOf(tmp));
                sb.append(formatTmp);
                sb.append(',');
            }
        }
        String lastItem = this.get(length - 1).toString();
        if (sc.isFloatString(lastItem)) {
            String fTmp = String.format("%.3f", Float.valueOf(lastItem));
            sb.append(fTmp);
        } else if (tc.checkType(lastItem).equals("string") || tc.checkType(lastItem).equals("int")) {
            sb.append(lastItem);
        }
        return sb.toString();
    }

    public void sbPrint() {
        if (this.equals(null)) {
            System.out.print("ERROR: Empty row.");
        }
        String rowRep = this.sbConcatRow();
        System.out.println(rowRep);
    }

    public void printRow() {
        if (this.equals(null)) {
            System.out.print("ERROR: Empty row.");
        }
        String rowString = this.convertRow();
        System.out.println(rowString);
    }


    public void addItem(T item) {
        this.row.add(item);
        this.length += 1;
    }

    public int getLength() {
        return this.length;
    }

    public T get(int index) {
        return row.get(index);
    }

    public static void main(String[] args) {
        Integer[] testArray = new Integer[]{1, 2, 3, 4, 5};
        Row<Integer> testRow = new Row(testArray);
        testRow.printRow();
    }
}
