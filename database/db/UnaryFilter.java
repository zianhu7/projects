package db;

import java.util.ArrayList;

/**
 * Created by Ethan Hu on 3/3/2017.
 */
public class UnaryFilter<T extends Comparable<? super T>> {
    private final TypeChecker tc = new TypeChecker();

    public ArrayList findFilter(Column x, T y, int compval) {
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

    public String findTypeComp(Column x, T value) {
        if (x.findType().equals("string")) {
            if (tc.checkType(value).equals("string")) {
                return "stringComp";
            } else {
                throw new RuntimeException("ERROR: Can't compare String type Column to non-String type literal.");
            }
        } else if (x.findType().equals("int") || x.findType().equals("float")) {
            if (tc.checkType(value).equals("int") || tc.checkType(value).equals("float")) {
                return "floatComp";
            } else {
                throw new RuntimeException("ERROR: Can't compare int/float type Column to String type literal.");
            }
        } else {
            throw new RuntimeException("ERROR: Invalid column or literal types for this database.");
        }
    }

    //Filters a Column, returning ArrayList of rowIndices with items in specified column that are greater than value.
    public ArrayList greaterFilter(Column x, T value) {
        ArrayList filtered = new ArrayList();
        for (int i = 1; i < x.size(); i += 1) {
            T item = (T) x.get(i);
            if (findTypeComp(x, value).equals("stringComp")) {
                if (item.compareTo(value) > 0) {
                    filtered.add(i);
                }
            } else if (findTypeComp(x, value).equals("floatComp")) {
                Float val1 = Float.valueOf(item.toString());
                Float val2 = Float.valueOf(value.toString());
                if (val1.compareTo(val2) > 0) {
                    filtered.add(i);
                }
            }

        }
        return filtered;
    }

    public ArrayList neFilter(Column x, T value) {
        ArrayList filtered = new ArrayList();
        for (int i = 1; i < x.size(); i += 1) {
            T item = (T) x.get(i);
            if (findTypeComp(x, value).equals("stringComp")) {
                if (item.compareTo(value) != 0) {
                    filtered.add(i);
                }
            } else if (findTypeComp(x, value).equals("floatComp")) {
                Float val1 = Float.valueOf(item.toString());
                Float val2 = Float.valueOf(value.toString());
                if (val1.compareTo(val2) != 0) {
                    filtered.add(i);
                }
            }

        }
        return filtered;
    }

    public ArrayList geFilter(Column x, T value) {
        ArrayList filtered = new ArrayList();
        for (int i = 1; i < x.size(); i += 1) {
            T item = (T) x.get(i);
            if (findTypeComp(x, value).equals("stringComp")) {
                if (item.compareTo(value) >= 0) {
                    filtered.add(i);
                }
            } else if (findTypeComp(x, value).equals("floatComp")) {
                Float val1 = Float.valueOf(item.toString());
                Float val2 = Float.valueOf(value.toString());
                if (val1.compareTo(val2) >= 0) {
                    filtered.add(i);
                }
            }

        }
        return filtered;
    }

    public ArrayList equalFilter(Column x, T value) {
        ArrayList filtered = new ArrayList();
        for (int i = 1; i < x.size(); i += 1) {
            T item = (T) x.get(i);
            if (findTypeComp(x, value).equals("stringComp")) {
                if (item.compareTo(value) == 0) {
                    filtered.add(i);
                }
            } else if (findTypeComp(x, value).equals("floatComp")) {
                Float val1 = Float.valueOf(item.toString());
                Float val2 = Float.valueOf(value.toString());
                if (val1.compareTo(val2) == 0) {
                    filtered.add(i);
                }
            }

        }
        return filtered;
    }

    //Same as greaterFilter, but for lesser values.
    public ArrayList lesserFilter(Column x, T value) {
        ArrayList filtered = new ArrayList();
        for (int i = 1; i < x.size(); i += 1) {
            T item = (T) x.get(i);
            if (findTypeComp(x, value).equals("stringComp")) {
                if (item.compareTo(value) < 0) {
                    filtered.add(i);
                }
            } else if (findTypeComp(x, value).equals("floatComp")) {
                Float val1 = Float.valueOf(item.toString());
                Float val2 = Float.valueOf(value.toString());
                if (val1.compareTo(val2) < 0) {
                    filtered.add(i);
                }
            }

        }
        return filtered;
    }

    public ArrayList leFilter(Column x, T value) {
        ArrayList filtered = new ArrayList();
        for (int i = 1; i < x.size(); i += 1) {
            T item = (T) x.get(i);
            if (findTypeComp(x, value).equals("stringComp")) {
                if (item.compareTo(value) <= 0) {
                    filtered.add(i);
                }
            } else if (findTypeComp(x, value).equals("floatComp")) {
                Float val1 = Float.valueOf(item.toString());
                Float val2 = Float.valueOf(value.toString());
                if (val1.compareTo(val2) <= 0) {
                    filtered.add(i);
                }
            }

        }
        return filtered;
    }
}
