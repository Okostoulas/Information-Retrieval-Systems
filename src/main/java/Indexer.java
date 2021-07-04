import model.MyDoc;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class Indexer {

    /**
     *  Indexes a document list with an English Analyzer and Classic similarity function
     * @param index_directory the directory on which the indexer should dump the indexed
     *                        corpus
     * @param myDocs the list of un-indexed documents
     * @param textField [REDUNDANT] the name of the text field used for categorization
     */
    public static void index(String index_directory, List<MyDoc> myDocs){

        try {
            long start_time = System.nanoTime();

            Directory directory = FSDirectory.open(Paths.get(index_directory));

            Analyzer analyzer = new EnglishAnalyzer();

            Similarity similarity = new ClassicSimilarity();

            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriterConfig.setSimilarity(similarity);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

            for (MyDoc myDoc : myDocs) {
                indexDoc(indexWriter, myDoc);
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
     */
    private static void indexDoc(IndexWriter indexWriter, MyDoc myDoc){

        try {
            Document doc = new Document();

            StoredField id = new StoredField("id", myDoc.getId());
            doc.add(id);

            //StoredField body = new StoredField("body", myDoc.getContent());
            //doc.add(body);

            FieldType type = new FieldType(TextField.TYPE_STORED);
            type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            type.setTokenized(true);
            type.setStored(true);
            type.setStoreTermVectors(true);
            type.setStoreTermVectorOffsets(true);
            type.setStoreTermVectorPositions(true);

            Field body = new Field("body", myDoc.getContent(), type);
            doc.add(body);

//            TextField content = new TextField(textField, myDoc.getContent(), Field.Store.NO);
//            doc.add(content);


            if (indexWriter.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                indexWriter.addDocument(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
