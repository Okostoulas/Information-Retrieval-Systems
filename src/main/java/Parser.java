import model.MyDoc;
import utils.Validators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class Parser {

    private String file_name;
    private String delimiter;
    private List<MyDoc> myDocuments;

    public Parser(String filename, String delimiter) {
        this.file_name = filename;
        this.delimiter = delimiter;
        this.myDocuments = new ArrayList<>();

    }

    public List<MyDoc> parse(){
        ReadFileToString();
        return myDocuments;
    }

    private void ReadFileToString() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file_name));
            String line_read;

            while ((line_read = reader.readLine()) != null) {

                String id;
                String text = "";

                if (Validators.isNumeric(line_read)) {

                    // parse first line of each text as id
                    id = line_read;

                    while (!(line_read = reader.readLine()).contains(delimiter)) {

                        // parse text as content while next line is not the delimiter
                        text = text.concat(line_read + " ");
                    }

                    // trim for extra spaces
                    text = text.trim().replaceAll(" +", " ");
                    MyDoc doc = new MyDoc(id, text);

                    myDocuments.add(doc);
                }
            }

            // CLOSE YO FILES !!!
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
