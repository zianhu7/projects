package db;

/**
 * Created by Ethan Hu on 3/6/2017.
 */
public class StringCheck {
    public boolean isIntString(String str) {
        if (str == null) {
            return false;
        }

        // check that input string is empty
        if (str.length() == 0) {
            return false;
        }

        // convert string into character array for ASCII code value checking
        char[] ch_arr = str.toCharArray();

        // check that first character is '+' or '-'
        if (ch_arr[0] == '+' || ch_arr[0] == '-') {
            // check that it's only sign
            if (ch_arr.length == 1) {
                return false;
            }

            // Otherwise, skip first character if they are '+' or '-' , then check the rest
            for (int i = 1; i < ch_arr.length; i++) {
                // check that characters consist of only [0, 9]
                if (ch_arr[i] < 48 || ch_arr[i] > 57) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < ch_arr.length; i++) {
                // check that characters consist of only [0, 9]
                if (ch_arr[i] < 48 || ch_arr[i] > 57) {
                    return false;
                }
            }
        }

        // return final check result
        return true;
    }

    public boolean isFloatString(String str) {
        if (str == null) {
            return false;
        }

        // check that input string is empty
        if (str.length() == 0) {
            return false;
        }

        // convert string into character array for ASCII code value checking
        char[] ch_arr = str.toCharArray();

        // check whether '.' and only one '.' exists
        boolean point_exists = false;
        for (int i = 0; i < ch_arr.length; i++) {
            if (ch_arr[i] == '.') {
                // if point is found for the first time
                if (point_exists == false) {
                    point_exists = true;
                } else {
                    // if more points are found
                    return false;
                }
            }
        }

        // if '.' doesn't exist
        if (point_exists == false) {
            return false;
        }

        // check that first character is '+' or '-'
        if (ch_arr[0] == '+' || ch_arr[0] == '-') {
            // check that it's only sign plus '.'
            if (ch_arr.length == 2) {
                return false;
            }

            // Otherwise, skip first character if they are '+' or '-' , then check the rest
            for (int i = 1; i < ch_arr.length; i++) {
                // skip '.' and continue checking the rest
                if (ch_arr[i] == '.') {
                    continue;
                }

                // check that characters consist of only [0, 9]
                else if (ch_arr[i] < 48 || ch_arr[i] > 57) {
                    // if it's the last character and it is 'f', it is acceptable
                    if (i == ch_arr.length - 1) {
                        if (ch_arr[i] != 'f') {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        } else {
            // check that it is only '.'
            if (ch_arr.length == 1) {
                return false;
            }

            for (int i = 0; i < ch_arr.length; i++) {
                // skip '.' and continue checking the rest
                if (ch_arr[i] == '.') {
                    continue;
                }

                // check that characters consist of only [0, 9]
                else if (ch_arr[i] < 48 || ch_arr[i] > 57) {
                    // if it's the last character and it is 'f', it is acceptable
                    if (i == ch_arr.length - 1) {
                        if (ch_arr[i] != 'f') {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }

        // return final check result
        return true;
    }


    public boolean isNameString(String str) {
        if (str == null) {
            return false;
        }

        // check that input string is empty
        if (str.length() == 0) {
            return false;
        }

        // convert all lower cases to upper ones for easy processing
        str = str.toUpperCase();

        // convert string into character array for ASCII code value checking
        char[] ch_arr = str.toCharArray();

        // make sure first symbol is a "'"
        if (ch_arr[0] != 39) {
            return false;
        }
/*
        // check whether input string consists of only [0, 9], [a, z], [A, Z] , "'", and '_'
        for (int i = 0; i < ch_arr.length; i++) {
            // skip '_' and continue checking the rest
            if (ch_arr[i] == ' ') {
                continue;
            }

            // check that characters consist of only [0, 9]
            else {
                if ((ch_arr[i] >= 48 && ch_arr[i] <= 57) || (ch_arr[i] >= 65 && ch_arr[i] <= 90) || ch_arr[i] == 39) {
                    continue;
                } else {
                    return false;
                }
            }*/
        if (ch_arr[ch_arr.length - 1] != 39) {
            return false;
        }


        // return final check result
        return true;
    }

    /*public static void main(String[] args) {
        // create one class instance
        StringCheck sc = new StringCheck();

        // check integer strings


        // check name strings
        System.out.println(sc.isNameString("apple orange"));
        System.out.println(sc.isNameString("'sdf'"));
        System.out.println(sc.isNameString("sdf"));
        System.out.println(sc.isNameString("129"));
    }*/
}

