import model.MyDoc;

import java.io.File;
import java.io.FileReader;
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

        String similarity_vectors_file = "Results/q_k_similarity_vectors_300.csv";
        List<List<MyDoc>> similarity_vectors;

        String index_without_queries_filename = "Results/data.csv";
        String index_with_queries_filename = "Results/data_with_queries.csv";
        /* END OF INITIAL SETUP */

        // mode selection
        switch (args[0]) {
            case "index":
                // Create results directory
                Parser.createResultsFile(results_directory_name);

                // Data parsing
                documents = Parser.parse(dataset_file, delimiter);
                queries = Parser.parse(queries_file, delimiter);

                // [SVD] index just the documents, in order to make the CSV without the queries
                System.out.println("Indexing dataset");
                Indexer.index(index_directory, documents, query_field);
                Indexer.compileVecDocAndWriteToCSV(index_directory, index_without_queries_filename);

                // [SVD] append queries to documents
                documents.addAll(queries);

                // [SVD] data parsing
                relevance_assessments = Parser.parse(relevance_assessments_file, delimiter);
                Parser.saveRelevanceAssessment(relevance_assessments, q_results_file);

                // [SVD] index WITH queries this time, in order to get the enriched terms
                System.out.println("Indexing dataset WITH queries");
                Indexer.index(index_directory, documents, query_field);
                Indexer.compileVecDocAndWriteToCSV(index_directory, index_with_queries_filename);
                break;
            case "search":
                // parse given CSV from Python IF EXISTS, otherwise exit
                File tempFile = new File(similarity_vectors_file);
                if (tempFile.exists()) {
                    // parse given CSV with qk similarity vectors
                    similarity_vectors = Parser.parseSimilarityVectorsCSV(similarity_vectors_file);
                    // Sort similarity vectors data in descending order
                    similarity_vectors = Searcher.sort_Similarity_Vectors(similarity_vectors);

                    // Searching
                    for (int k : k_results) {
                        System.out.println("Executing queries and getting top " + k + " documents");
                        Searcher.executeQueries(index_directory, query_field, similarity_vectors, k);
                    }
                } else {
                    System.out.println("Similarity vectors \"q_k_similarity_vectors.csv\" file does not exist!");
                    System.out.println("Make sure you have it in the Results folder or Run the Python script to generate it");
                }
                break;
            default:
                break;
        }

    }

}
