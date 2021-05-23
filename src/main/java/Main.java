import model.MyDoc;

import java.util.Collections;
import java.util.Comparator;
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

        String similarity_vectors_file = "Results/q_k_similarity_vectors.csv";
        List<List<MyDoc>> similarity_vectors;
        /* END OF INITIAL SETUP */

        // Create results directory
        Parser.createResultsFile(results_directory_name);

        // Data parsing
        documents = Parser.parse(dataset_file, delimiter);
        queries = Parser.parse(queries_file, delimiter);

        // [SVD] append queries to documents
        documents.addAll(queries);
        relevance_assessments = Parser.parse(relevance_assessments_file, delimiter);

        Parser.saveRelevanceAssessment(relevance_assessments, q_results_file);

        // Indexing
        System.out.println("Indexing dataset and queries");
        Indexer.index(index_directory, documents, query_field);

        // Reading the Index
        /*
        try {
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index_directory)));
            // print Vector Index
            Indexer.printIndex(indexReader);

            Double[][] vector = Indexer.getSparseVecArray(indexReader);

            // write vector index to csv
            Indexer.writeSparseVecArrayToCSV(vector);

        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        // parse given CSV with qk similarity vectors
        similarity_vectors = Parser.parseSimilarityVectorsCSV(similarity_vectors_file);
        similarity_vectors = Searcher.sort_Similarity_Vectors(similarity_vectors);

        // Searching
        for (int k : k_results){
            System.out.println("Executing queries and getting top " + k + " documents");
            Searcher.executeQueries(index_directory, query_field, similarity_vectors, k);
        }


    }


}
