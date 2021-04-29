import model.MyDoc;
import utils.Validators;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    /**
     * Calls methods that parse and test the dataset
     * Exits if problems were found in the dataset
     * @return the list of documents parsed
     */
    public static List<MyDoc> parse(String filename, String delimiter){
        List<MyDoc> documents;
        documents = addDataToList(filename, delimiter);

        if (!runChecks(documents)){
            System.exit(4);
        }

        return documents;
    }

    /**
     * Reads data from dataset and arranges it into MyDoc objects
     * Then adds all the data into the myDocuments list
     */
    private static List<MyDoc> addDataToList(String filename, String delimiter) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line_read;
            List<MyDoc> list = new ArrayList<>();

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
                    list.add(doc);
                }
            }
            // CLOSE YO FILES !!!
            reader.close();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks the ids and the contents of each document
     * @param documents the list of documents to be checked
     * @return true if all tests passed
     */
    private static boolean runChecks(List<MyDoc> documents){
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
    private static boolean listIdsArePresent(List<MyDoc> documents){
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
    private static boolean noContentsWarn(List<MyDoc> documents){
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


    public static void saveRelevanceAssessment(List<MyDoc> relevance_assessments){
        try {
            File file = new File("qrels.txt");
            FileWriter fileWriter = new FileWriter(file);
            for (MyDoc doc : relevance_assessments){
                String[] doc_ids = doc.getContent().split(" ");

                for (String doc_id : doc_ids){
                    fileWriter.write("Q" + doc.getId() + " " + doc_id + " 1\n");
                }

            }

            fileWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

}
