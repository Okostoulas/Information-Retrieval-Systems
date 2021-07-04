import model.MyDoc;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.deeplearning4j.models.word2vec.Word2Vec;

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
    public static void executeQueries(String index_directory, String field, List<MyDoc> queries, int k, Word2Vec vec, Analyzer analyzer){

        try {
            long start_time = System.nanoTime();

            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index_directory)));

            //printIndex(indexReader);

            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(new WordEmbeddingsSimilarity(vec,
                    field,
                    WordEmbeddingsSimilarity.Smoothing.MEAN));


            QueryParser queryParser = new QueryParser(field, analyzer);

            File file = new File("Results/first_" + k + "_results.txt");
            FileWriter fileWriter = new FileWriter(file);

            for (MyDoc doc_query : queries) {
                Query query = queryParser.parse(doc_query.getContent());
                //System.out.println(query.toString(field));

                // Get top k results
                TopDocs results = indexSearcher.search(query, k);
                ScoreDoc[] hits = results.scoreDocs;

                // Save results
                for (ScoreDoc hit : hits) {
                    if (hit.doc < 11429){
                        Document hitDoc = indexSearcher.doc(hit.doc);
                        fileWriter.write(doc_query.getId() + "\t0\t" + hitDoc.get("id") + "\t0\t" + hit.score + "\tW2V\n");
                    }
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

    /**
     * Prints the index word by word
     * @param reader the Index Reader
     */
    private static void printIndex(IndexReader reader) throws IOException {
        final Fields fields = MultiFields.getFields(reader);

        for (String field : fields) {
            final Terms terms = MultiFields.getTerms(reader, field);
            final TermsEnum it = terms.iterator();
            BytesRef term = it.next();
            while (term != null) {
                System.out.print(term.utf8ToString() + ", ");
                term = it.next();
            }
        }
    }

    public static Analyzer createAnalyzer(Word2Vec vec){
        // Create custom analyzer
        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String field) {
                Tokenizer tokenizer = new WhitespaceTokenizer();
                double minAcc = 0.95;

                TokenFilter synFilter = new W2VSynonymFilter(tokenizer, vec, minAcc);
                return new TokenStreamComponents(tokenizer, synFilter);
            }
        };
    }
}
