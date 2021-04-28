import model.MyDoc;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Indexer {

    List<MyDoc> myDocs;
    public static final String index_directory = ".index";

    public Indexer(List<MyDoc> myDocs) {
        this.myDocs = myDocs;
    }

    /**
     *  Indexes a document list with an English Analyzer and BM25 similarity function
     */
    public void index(){

        try {
            Directory directory = FSDirectory.open(Paths.get(index_directory));

            Analyzer analyzer = new EnglishAnalyzer();

            Similarity similarity = new BM25Similarity();

            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriterConfig.setSimilarity(similarity);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

            for (MyDoc myDoc : myDocs) {
                indexDoc(indexWriter, myDoc);
            }
            // CLOSE YO index writers!
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a document and adds it in the index
     * @param indexWriter the index writer used
     * @param myDoc the document that gets added to the index
     */
    private void indexDoc(IndexWriter indexWriter, MyDoc myDoc){

        try {
            Document doc = new Document();

            TextField id = new TextField("id", myDoc.getId(), Field.Store.YES);
            doc.add(id);
            TextField content = new TextField("content", myDoc.getContent(), Field.Store.NO);
            doc.add(content);

            if (indexWriter.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                indexWriter.addDocument(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
