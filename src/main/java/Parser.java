import com.opencsv.CSVReader;
import model.MyDoc;
import utils.Validators;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Parser {

    /**
     * Calls methods that parse and test the dataset
     * Exits if problems were found in the dataset
     * @param filename the location of the file
     * @param delimiter the basic delimiter used
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
     * @param filename the location of the file
     * @param delimiter the basic delimiter used
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
                    text = text.trim().replaceAll(" +", " ").toLowerCase(Locale.ROOT);

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


    /**
     * Converts the relevance assessment file contents into a format that can be recognised by trec_eval
     * @param relevance_assessments the contents of the relevance assessment file
     * @param filename the name of the file to read
     */
    public static void saveRelevanceAssessment(List<MyDoc> relevance_assessments, String filename){
        try {
            File file = new File(filename);
            FileWriter fileWriter = new FileWriter(file);
            for (MyDoc doc : relevance_assessments){
                String[] doc_ids = doc.getContent().split(" ");

                for (String doc_id : doc_ids){
                    fileWriter.write(doc.getId() + "\t0\t" + doc_id + "\t1\n");
                }
            }

            fileWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Checks if results directory exist and creates it if absent
     * @param results_directory_name the name of the results directory
     */
    public static void createResultsFile(String results_directory_name){
        File results_file_directory = new File(results_directory_name);
        if (!results_file_directory.isDirectory()){
            System.out.println("Creating results directory..");
            results_file_directory.mkdir();
        }
    }

    /**
     * Reads the similarity vector csv line by line
     * Adds each row's data to a List<MyDoc> and then adds it to the vectors List of Lists
     * @param filename the similarity vector file name
     * @return it's contents as a List<List<MyDoc>> object
     */
    public static List<List<MyDoc>> parseSimilarityVectorsCSV(String filename) {

        List<List<MyDoc>> vectors = new ArrayList<>();

        try {
            // Create an object of filereader
            // class with CSV file as a parameter.
            FileReader filereader = new FileReader(filename);

            // create csvReader object passing
            // file reader as a parameter
            CSVReader csvReader = new CSVReader(filereader);
            String[] nextRecord;

            // we are going to read data line by line
            while ((nextRecord = csvReader.readNext()) != null) {
                int i = 0;
                List<MyDoc> docs = new ArrayList<>();
                for (String cell : nextRecord) {
                    docs.add(new MyDoc(Integer.toString(i), cell));
                    i++;
                }
                vectors.add(docs);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return vectors;
    }
}
