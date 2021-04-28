import model.MyDoc;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        String delimiter = "/";
        String dataset = "Dataset/doc-text";
        List<MyDoc> myDocs;
        
        // Data processing
        Parser parser = new Parser(dataset, delimiter);
        myDocs = parser.parse();

        // Indexing
        Indexer indexer = new Indexer(myDocs);
        indexer.index();

    }
}
