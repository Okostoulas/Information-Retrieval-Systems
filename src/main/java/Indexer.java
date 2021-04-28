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

import java.util.List;

public class Indexer {

    List<MyDoc> myDocs;

    public Indexer(List<MyDoc> myDocs) {
        this.myDocs = myDocs;
    }

    public void index(){
        for (MyDoc myDoc : myDocs) {
            System.out.println(myDoc);
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

            TextField id = new TextField("id", myDoc.getId(), Field.Store.NO);
            doc.add(id);
            StoredField content = new StoredField("content", myDoc.getContent());
            doc.add(content);

            if (indexWriter.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                System.out.println("Adding document with id: " + myDoc.getId());
                indexWriter.addDocument(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
