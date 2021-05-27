import model.MyDoc;
import org.apache.lucene.search.similarities.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        /* INITIAL SETUP */
        String delimiter = "/";
        String dataset_file = "Dataset/doc-text";
        String queries_file = "Dataset/query-text";
        String relevance_assessments_file = "Dataset/rlv-ass";
        String results_directory_name = "Results";
        String index_directory = "./index";
        String query_field = "content";
        String q_results_file = "Results/qrels.txt";
        int[] k_results = {5, 10, 15, 20, 30, 50};
        List<MyDoc> documents;
        List<MyDoc> queries;
        List<MyDoc> relevance_assessments;

        // BM25
        float BMk = 0.6F;
        float BMb = 0.75F;

        // LM
        float lamda = 1.0F;

        /* END OF INITIAL SETUP */

        // Create results directory
        Parser.createResultsFile(results_directory_name);

        // Data parsing
        documents = Parser.parse(dataset_file, delimiter);
        queries = Parser.parse(queries_file, delimiter);
        relevance_assessments = Parser.parse(relevance_assessments_file, delimiter);

        Parser.saveRelevanceAssessment(relevance_assessments, q_results_file);

        // Indexing
        Similarity sim;

        switch (args[0]) {
            case "bm25":
                System.out.println("Applying BM25 similarity...");
                sim = new BM25Similarity(BMk, BMb);
                break;
            case "lmdir":
                System.out.println("Applying Language Model similarity with Dirichlet smoothing...");
                sim = new LMDirichletSimilarity();
                break;
            case "lmjelmer":
                System.out.println("Applying Language Model similarity with Jelinek Mercer smoothing...");
                sim = new LMJelinekMercerSimilarity(lamda);
                break;
            default:
                System.out.println("Applying Classic Similarity...");
                sim = new ClassicSimilarity();
                break;
        }

        System.out.println("Indexing dataset");
        Indexer.index(index_directory, documents, query_field);

        // Searching
        for (int k : k_results){
            System.out.println("Executing queries and getting top " + k + " documents");
            Searcher.executeQueries(index_directory, query_field, queries, k);
        }


    }
}
