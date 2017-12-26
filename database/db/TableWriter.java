package db;

import java.io.IOException;
import java.io.*;

/**
 * Created by Ethan Hu on 3/6/2017.
 */
public class TableWriter {
    public String tableWriter(String filename, String[] tableContent) throws IOException {
        try {
            if (filename.equals(null)) {
                throw new RuntimeException("RuntimeException thrown: filename is null.");
                //Instead of above return, write in throw/catch blocks to catch exceptions.
            }
            if (tableContent.equals(null)) {
                throw new RuntimeException("RuntimeException thrown: content of table is empty.");
                //Same here, come back to change/refactor.
            }
            for (int i = 0; i < tableContent.length; i += 1) {
                tableContent[i] = tableContent[i] + "\n";
            }

            FileWriter fw = new FileWriter(filename);
            BufferedWriter bw = new BufferedWriter(fw);
            for (int j = 0; j < tableContent.length; j += 1) {
                bw.write(tableContent[j], 0, tableContent[j].length());
            }
            bw.flush();
            bw.close();
            return "";
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
