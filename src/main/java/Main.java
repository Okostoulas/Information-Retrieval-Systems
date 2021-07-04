import model.MyDoc;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;
import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.IOException;
import java.nio.file.Paths;
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
        String q_results_file = "Results/qrels.txt";
        String nn_model = "Dataset/model.txt";
        int[] k_results = {5, 10, 15, 20, 30, 50};
        List<MyDoc> documents;
        List<MyDoc> queries;
        List<MyDoc> relevance_assessments;
        Word2Vec vec = new Word2Vec();
        /* END OF INITIAL SETUP */

        // Create results directory
        Parser.createResultsFile(results_directory_name);

        // Data parsing
        documents = Parser.parse(dataset_file, delimiter);
        queries = Parser.parse(queries_file, delimiter);
        relevance_assessments = Parser.parse(relevance_assessments_file, delimiter);

        Parser.saveRelevanceAssessment(relevance_assessments, q_results_file);

        // Indexing
        System.out.println("Indexing dataset");
        Indexer.index(index_directory, documents);

        // NN Training
        IndexReader indexReader = null;
        try {
            indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index_directory)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        FieldValuesSentenceIterator iterator = new FieldValuesSentenceIterator(indexReader, "body");

        // Config Switch mode
        switch (args[0]){
            case "fit":
                System.out.println("Running fit model");
                vec = new Word2Vec.Builder()
                        .layerSize(100)
                        .windowSize(3)
                        .elementsLearningAlgorithm(new CBOW<>())
                        .tokenizerFactory(new LuceneTokenizerFactory(new StandardAnalyzer()))
                        .iterate(iterator)
                        .build();

                vec.fit();
                break;
            case "wikipedia":
                System.out.println("Running wikipedia model");
                vec = WordVectorSerializer.readWord2VecModel(nn_model);
                break;
            default:
                System.out.println("No arguments were given, running fit model.");
                vec = new Word2Vec.Builder()
                        .layerSize(100)
                        .windowSize(3)
                        .elementsLearningAlgorithm(new CBOW<>())
                        .tokenizerFactory(new LuceneTokenizerFactory(new StandardAnalyzer()))
                        .iterate(iterator)
                        .build();

                vec.fit();
                break;
        }


        // Searching
        for (int k : k_results){
            System.out.println("###############\nExecuting queries and getting top " + k + " documents\n###############");
            // create analyzer once
            Analyzer analyzer = Searcher.createAnalyzer(vec);
            Searcher.executeQueries(index_directory, "body", queries, k, vec, analyzer);
        }


    }
}
