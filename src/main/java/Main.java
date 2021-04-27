import model.MyDoc;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        String delimiter = "/";
        String dataset = "Dataset/doc-text";

        // Data processing
        Parser parser = new Parser(dataset, delimiter);

        List<MyDoc> temp;
        try {
            temp = parser.parse();
            System.out.println(temp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Indexing

    }
}
