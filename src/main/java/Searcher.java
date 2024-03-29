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
import java.util.List;

public class Searcher {

    /**
     * Executes the list of queries given, assesses results per given number of first
     * result, and saves the results to "Results/" dir
     * @param index_directory the dir of the indexed corpus
     * @param field the field on which the searcher will search
     * @param queries the list of queries
     * @param k the number of first results
     */
    public static void executeQueries(String index_directory, String field, List<MyDoc> queries, int k){

        try {
            long start_time = System.nanoTime();

            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index_directory)));
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(new ClassicSimilarity());

            Analyzer analyzer = new EnglishAnalyzer();

            QueryParser queryParser = new QueryParser(field, analyzer);

            File file = new File("Results/first_" + k + "_results.txt");
            FileWriter fileWriter = new FileWriter(file);

            for (MyDoc doc_query : queries) {
                Query query = queryParser.parse(doc_query.getContent());

                // Get top k results
                TopDocs results = indexSearcher.search(query, k);
                ScoreDoc[] hits = results.scoreDocs;

                // Save results
                for (ScoreDoc hit : hits) {
                    Document hitDoc = indexSearcher.doc(hit.doc);
                    fileWriter.write(doc_query.getId() + "\t0\t" + hitDoc.get("id") + "\t0\t" + hit.score + "\tVEC_SPACE\n");
                }
            }

            // CLOSE YO READERS AND YO FILES
            indexReader.close();
            fileWriter.close();

            // Time elapsed calculation
            long end_time = System.nanoTime();
            long time_elapsed = end_time - start_time;
            double time_elapsed_converted = (double) time_elapsed / 1_000_000_000.0;
            System.out.println("Time to search queries: " + time_elapsed_converted + " seconds and return the top : " + k + " documents");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

}
