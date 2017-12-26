package db;

import java.io.IOException;
import java.io.*;

/**
 * Created by Ethan Hu on 3/6/2017.
 */
public class TableReader {
    public String[] tableReader(String filename) throws IOException {
        FileReader fr = new FileReader(filename);
        BufferedReader reader = new BufferedReader(fr);
        reader.mark(1000000);
        int numLines = 0;
        String tmp = "";
        try {
            while (tmp != null) {
                tmp = reader.readLine();
                if (tmp != null) {
                    numLines += 1;
                }
            }
        } catch (IOException e) {
            if (numLines == 0) {
                throw new RuntimeException("ERROR: The file is empty.");
            }
        }
        String[] tableContent = new String[numLines];
        reader.reset();
        int counter = 0;
        while (counter < numLines) {
            tableContent[counter] = reader.readLine();
            counter += 1;
        }
        reader.close();
        return tableContent;
    }
}
