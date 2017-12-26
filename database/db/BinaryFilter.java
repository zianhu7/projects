package db;

import org.omg.SendingContext.RunTime;

import java.util.*;

/**
 * Created by Ethan Hu on 3/3/2017.
 */
public class BinaryFilter<T extends Comparable<? super T>> {
    public ArrayList findFilter(Column x, Column y, int compval) {
        if (compval == -2) {
            return lesserFilter(x, y);
        } else if (compval == -1) {
            return leFilter(x, y);
        } else if (compval == 0) {
            return equalFilter(x, y);
        } else if (compval == 1) {
            return geFilter(x, y);
        } else if (compval == 2) {
            return greaterFilter(x, y);
        } else if (compval == 10) {
            return neFilter(x, y);
        } else {
            throw new RuntimeException("Please provide a valid comparison value.");
        }
    }

    public String findTypeComp(Column x, Column y) {
        if (x.findType().equals("string")) {
            if (y.findType().equals("string")) {
                return "stringComp";
            } else {
                throw new RuntimeException("ERROR: Can't compare String type Column to non-String type literal.");
            }
        } else if (x.findType().equals("int") || x.findType().equals("float")) {
            if (y.findType().equals("int") || y.findType().equals("float")) {
                return "floatComp";
            } else {
                throw new RuntimeException("ERROR: Can't compare int/float type Column to String type literal.");
            }
        } else {
            throw new RuntimeException("ERROR: Invalid column or literal types for this database.");
        }
    }

    //Returns ArrayList of Row indices in table s.t. column x's values are greater than y's values.
    public ArrayList greaterFilter(Column x, Column y) {
        if (x.size() != y.size()) {
            throw new RuntimeException("Columns must be of same size to be compared & filtered.");
        }
        ArrayList<Integer> filteredRV = new ArrayList<>();
        for (int i = 1; i < x.size(); i += 1) {
            T item1 = (T) x.get(i);
            T item2 = (T) y.get(i);
            if (findTypeComp(x, y).equals("stringComp")) {
                if (item1.compareTo(item2) > 0) {
                    filteredRV.add(i);
                }
            } else if (findTypeComp(x, y).equals("floatComp")) {
                Float val1 = Float.valueOf(item1.toString());
                Float val2 = Float.valueOf(item2.toString());
                if (val1.compareTo(val2) > 0) {
                    filteredRV.add(i);
                }
            }

        }
        return filteredRV;
    }

    public ArrayList neFilter(Column x, Column y) {
        if (x.size() != y.size()) {
            throw new RuntimeException("Columns must be of same size to be compared & filtered.");
        }
        ArrayList<Integer> filteredRV = new ArrayList<>();
        for (int i = 1; i < x.size(); i += 1) {
            T item1 = (T) x.get(i);
            T item2 = (T) y.get(i);
            if (findTypeComp(x, y).equals("stringComp")) {
                if (item1.compareTo(item2) != 0) {
                    filteredRV.add(i);
                }
            } else if (findTypeComp(x, y).equals("floatComp")) {
                Float val1 = Float.valueOf(item1.toString());
                Float val2 = Float.valueOf(item2.toString());
                if (val1.compareTo(val2) != 0) {
                    filteredRV.add(i);
                }
            }

        }
        return filteredRV;
    }

    //Returns ArrayList of Row indices in table s.t. column x's values are greater than OR equal to y's values.
    public ArrayList geFilter(Column x, Column y) {
        if (x.size() != y.size()) {
            throw new RuntimeException("Columns must be of same size to be compared & filtered.");
        }
        ArrayList<Integer> filteredRV = new ArrayList<>();
        for (int i = 1; i < x.size(); i += 1) {
            T item1 = (T) x.get(i);
            T item2 = (T) y.get(i);
            if (findTypeComp(x, y).equals("stringComp")) {
                if (item1.compareTo(item2) >= 0) {
                    filteredRV.add(i);
                }
            } else if (findTypeComp(x, y).equals("floatComp")) {
                Float val1 = Float.valueOf(item1.toString());
                Float val2 = Float.valueOf(item2.toString());
                if (val1.compareTo(val2) >= 0) {
                    filteredRV.add(i);
                }
            }

        }
        return filteredRV;
    }

    public ArrayList lesserFilter(Column x, Column y) {
        if (x.size() != y.size()) {
            throw new RuntimeException("Columns must be of same size to be compared & filtered.");
        }
        ArrayList<Integer> filteredRV = new ArrayList<>();
        for (int i = 1; i < x.size(); i += 1) {
            T item1 = (T) x.get(i);
            T item2 = (T) y.get(i);
            if (findTypeComp(x, y).equals("stringComp")) {
                if (item1.compareTo(item2) < 0) {
                    filteredRV.add(i);
                }
            } else if (findTypeComp(x, y).equals("floatComp")) {
                Float val1 = Float.valueOf(item1.toString());
                Float val2 = Float.valueOf(item2.toString());
                if (val1.compareTo(val2) < 0) {
                    filteredRV.add(i);
                }
            }

        }
        return filteredRV;
    }

    //Returns ArrayList of Row indices in table s.t. column x's values are greater than OR equal to y's values.
    public ArrayList leFilter(Column x, Column y) {
        if (x.size() != y.size()) {
            throw new RuntimeException("Columns must be of same size to be compared & filtered.");
        }
        ArrayList<Integer> filteredRV = new ArrayList<>();
        for (int i = 1; i < x.size(); i += 1) {
            T item1 = (T) x.get(i);
            T item2 = (T) y.get(i);
            if (findTypeComp(x, y).equals("stringComp")) {
                if (item1.compareTo(item2) <= 0) {
                    filteredRV.add(i);
                }
            } else if (findTypeComp(x, y).equals("floatComp")) {
                Float val1 = Float.valueOf(item1.toString());
                Float val2 = Float.valueOf(item2.toString());
                if (val1.compareTo(val2) <= 0) {
                    filteredRV.add(i);
                }
            }

        }
        return filteredRV;
    }

    public ArrayList equalFilter(Column x, Column y) {
        if (x.size() != y.size()) {
            throw new RuntimeException("Columns must be of same size to be compared & filtered.");
        }
        ArrayList<Integer> filteredRV = new ArrayList<>();
        for (int i = 1; i < x.size(); i += 1) {
            T item1 = (T) x.get(i);
            T item2 = (T) y.get(i);
            if (findTypeComp(x, y).equals("stringComp")) {
                if (item1.compareTo(item2) == 0) {
                    filteredRV.add(i);
                }
            } else if (findTypeComp(x, y).equals("floatComp")) {
                Float val1 = Float.valueOf(item1.toString());
                Float val2 = Float.valueOf(item2.toString());
                if (val1.compareTo(val2) == 0) {
                    filteredRV.add(i);
                }
            }

        }
        return filteredRV;
    }

}
