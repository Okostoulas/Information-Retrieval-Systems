import model.MyDoc;
import utils.Validators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final String file_name;
    private final String delimiter;
    private final List<MyDoc> myDocuments;

    public Parser(String filename, String delimiter) {
        this.file_name = filename;
        this.delimiter = delimiter;
        this.myDocuments = new ArrayList<>();

    }

    /**
     * Calls methods that parse and test the dataset
     * Exits if problems were found in the dataset
     * @return the list of documents parsed
     */
    public List<MyDoc> parse(){
        addDocsToList();

        if (!runChecks(myDocuments)){
            System.exit(4);
        }

        return myDocuments;
    }

    private void addDocsToList() {
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

                    // add document to list of documents
                    myDocuments.add(doc);
                }
            }
            // CLOSE YO FILES !!!
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks the ids and the contents of each document
     * @param documents the list of documents to be checked
     * @return true if all tests passed
     */
    private boolean runChecks(List<MyDoc> documents){
        int flag = 0;

        if (!listIdsArePresent(documents)){
            flag++;
        }
        if (!noContentsWarn(documents)){
            flag = flag + 2;
        }

        switch (flag) {
            case 1:
                System.out.println("ERROR: Found miss-matching ids on one or more documents. Closing...");
                return false;
            case 2:
                System.out.println("WARNING: Found documents with empty contents");
                return true;
            case 3:
                System.out.println("WARNING: Found documents with empty contents");
                System.out.println("ERROR: Found miss-matching ids on one or more documents. Closing...");
                return false;
            default:
                return true;
        }
    }

    /**
     * Checks the validity of the ids
     * @param documents the list to be tested
     * @return  true if ids represent the corpus correctly
     */
    private boolean listIdsArePresent(List<MyDoc> documents){
        int flag = 0;
        for (int i = 0; i < (documents.size()); i++) {
            if (i + 1 != Integer.parseInt(documents.get(i).getId())){
                System.out.println("Searched for id: " + i);
                System.out.println("Found id: " + documents.get(i).getId());
                flag++;
            }
        }
        return flag == 0;
    }

    /**
     * Checks if any document is empty
     * @param documents the list to be tested
     * @return true if all documents have content
     */
    private boolean noContentsWarn(List<MyDoc> documents){
        int flag = 0;
        for (MyDoc doc : documents) {
            if (doc.getContent().equals("")){
                System.out.println("WARNING: Document with id: " + doc.getId() + " has no content.");
                flag++;
            }
        }
        if (flag > 0) System.out.println("WARNING: Found " + flag + "cases with missing content.");
        return flag == 0;
    }

}
