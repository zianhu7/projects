package db;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by Ethan Hu on 3/4/2017.
 */

//Annotation: class needs revision, or EVERY time you call it upon an actual parameter you will need to (T) type cast.
public class TypeChecker<T extends Comparable<? super T>> {

    public String checkType(T item) {
        if (item.getClass().equals(Integer.class)) {
            return "int";
        } else if (item.getClass().equals(Float.class)) {
            return "float";
        } else if (item.getClass().equals(String.class)) {
            return "string";
        } else {
            return "ERROR: Malformed type";
        }
    }

    /*@Test
    public void testCheckType() {
        ArrayList a = new ArrayList();
        String x = "title";
        Float y = 245.125f;
        Integer z = 3;
        a.add(x);
        a.add(y);
        a.add(z);
        for (int i = 0; i < a.size(); i += 1) {
            System.out.println(checkType((T) a.get(i)));
        }
    }*/
}
