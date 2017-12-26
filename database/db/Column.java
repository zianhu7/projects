package db;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import javax.print.DocFlavor;
import java.util.*;

/**
 * Created by Ethan Hu on 2/27/2017.
 */
public class Column<T extends Comparable<? super T>> {
    private ArrayList<T> col;
    private String title;
    private String name;
    private int size;
    private String type;
    private final TypeChecker tc = new TypeChecker();

    //Takes in pre-checked Strings (header, type) following specs and converted with StringBuffer.toString() as an ex.
    public Column(String name, String type) {
        type = type.toLowerCase();
        col = new ArrayList<>();
        this.name = name;
        this.title = name + " " + type;
        setType(type);
        col.add((T) this.title);
        size = 1;
    }

    public String findName() {
        return this.name;
    }

    public void setType(String type) {
        type = type.toLowerCase();
        if (type.equals("float")) {
            this.type = "float";
        } else if (type.equals("string")) {
            this.type = "string";
        } else if (type.equals("int")) {
            this.type = "int";
        } else {
            throw new RuntimeException("ERROR: Please enter a valid type for the table " +
                    "from the 3 primitives (string, float, int).");
        }
    }

    public String findTitle() {
        return this.title;

    }

    public void addItem(T x) {
        if (!tc.checkType(x).equals(this.type)) {
            throw new RuntimeException("ERROR: Can only add values with same type as the column.");
        }
        this.col.add(x);
        this.size += 1;
    }

    public String findType() {
        return this.type;
    }


    public int size() {
        return this.size;
    }


    public T get(int index) {
        return this.col.get(index);
    }


/*
    public static void main(String[] args) {
        Column t1 = new Column("X", "int");
        t1.addItem(3);
        System.out.println(t1.get(1));
    }*/
}

