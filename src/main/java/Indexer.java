import model.MyDoc;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Indexer {

    /**
     *  Indexes a document list with an English Analyzer and Classic similarity function
     * @param index_directory the directory on which the indexer should dump the indexed
     *                        corpus
     * @param myDocs the list of un-indexed documents
     * @param textField [REDUNDANT] the name of the text field used for categorization
     * @param sim   the similarity model to be used
     */
    public static void index(String index_directory, List<MyDoc> myDocs, String textField, Similarity sim){

        try {
            long start_time = System.nanoTime();

            Directory directory = FSDirectory.open(Paths.get(index_directory));

            Analyzer analyzer = new EnglishAnalyzer();

            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriterConfig.setSimilarity(sim);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

            for (MyDoc myDoc : myDocs) {
                indexDoc(indexWriter, myDoc, textField);
            }
            // CLOSE YO index writers!
            indexWriter.close();

            // Time elapsed calculation
            long end_time = System.nanoTime();
            long time_elapsed = end_time - start_time;
            double time_elapsed_converted = (double) time_elapsed / 1_000_000_000.0;
            System.out.println("Time to create index: " + time_elapsed_converted + " seconds");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a document and adds it in the index
     * @param indexWriter the index writer used
     * @param myDoc the document that gets added to the index
     * @param textField specifies which text field to name and use
     */
    private static void indexDoc(IndexWriter indexWriter, MyDoc myDoc, String textField){

        try {
            Document doc = new Document();

            TextField id = new TextField("id", myDoc.getId(), Field.Store.YES);
            doc.add(id);
            TextField content = new TextField(textField, myDoc.getContent(), Field.Store.NO);
            doc.add(content);

            if (indexWriter.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                indexWriter.addDocument(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
