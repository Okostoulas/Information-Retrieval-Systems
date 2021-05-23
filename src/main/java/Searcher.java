import model.MyDoc;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Searcher {

    /**
     * Executes the list of queries given, assesses results per given number of first
     * result, and saves the results to "Results/" dir
     * @param index_directory the dir of the indexed corpus
     * @param field the field on which the searcher will search
     * @param k the number of first results
     */
    public static void executeQueries(String index_directory, String field, List<List<MyDoc>> similarity_vectors, int k){

        try {
            long start_time = System.nanoTime();

            // -----------------------
            // [SVD] Get top k results
            // -----------------------


            File file = new File("Results/first_" + k + "_results.txt");
            FileWriter fileWriter = new FileWriter(file);

            int docId = 1;
            for (List<MyDoc> doc_query : similarity_vectors) {

                // Save results
                for (int i = 0; i < k; i++) {
                    MyDoc hitDoc = doc_query.get(i);
                    fileWriter.write(docId + "\t0\t" + hitDoc.getId() + "\t0\t" + hitDoc.getContent() + "\tLSI_SVD\n");
                }
                docId++;
            }

            // CLOSE YO READERS AND YO FILES
            fileWriter.close();

            // Time elapsed calculation
            long end_time = System.nanoTime();
            long time_elapsed = end_time - start_time;
            double time_elapsed_converted = (double) time_elapsed / 1_000_000_000.0;
            System.out.println("Time to search queries: " + time_elapsed_converted + " seconds and return the top : " + k + " documents");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Sorts [DESCENDING] a list of Mydoc lists by the contents (here compares as doubles)
     * @param similarity_vectors    the MyDoc list
     * @return  sorted list of Mydoc lists
     */
    public static List<List<MyDoc>> sort_Similarity_Vectors(List<List<MyDoc>> similarity_vectors) {
        for (int i = 0; i <= similarity_vectors.size() - 1; i++) {
            Collections.sort(similarity_vectors.get(i), new Comparator<MyDoc>() {
                public int compare(MyDoc s1, MyDoc s2) {
                    return Double.compare(Double.parseDouble(s2.getContent()), Double.parseDouble(s1.getContent()));
                }
            });
        }
        return similarity_vectors;
    }

}
