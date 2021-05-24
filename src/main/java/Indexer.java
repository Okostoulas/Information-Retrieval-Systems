import model.MyDoc;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.IndexSearcher;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import com.opencsv.CSVWriter;

import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.classification.utils.DocToDoubleVectorUtils;


public class Indexer {

    /**
     *  Indexes a document list with an English Analyzer and Classic similarity function
     * @param index_directory the directory on which the indexer should dump the indexed
     *                        corpus
     * @param myDocs the list of un-indexed documents
     * @param textField [REDUNDANT] the name of the text field used for categorization
     */
    public static void index(String index_directory, List<MyDoc> myDocs, String textField){

        try {
            long start_time = System.nanoTime();

            Directory directory = FSDirectory.open(Paths.get(index_directory));

            Analyzer analyzer = new EnglishAnalyzer();

            Similarity similarity = new ClassicSimilarity();

            FieldType type = createFieldType();

            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriterConfig.setSimilarity(similarity);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

            for (MyDoc myDoc : myDocs) {
                indexDoc(indexWriter, myDoc, textField, type);
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
     * Creates a Field type so it can be used for the index
     * @return the field type
     */
    private static FieldType createFieldType(){
        FieldType type = new FieldType();
        type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        type.setTokenized(true);
        type.setStored(true);
        type.setStoreTermVectors(true);

        return type;
    }

    /**
     * Creates a document and adds it in the index
     * @param indexWriter the index writer used
     * @param myDoc the document that gets added to the index
     * @param textField specifies which text field to name and use
     */
    static void indexDoc(IndexWriter indexWriter, MyDoc myDoc, String textField, FieldType type){

        try {
            Document doc = new Document();

            TextField id = new TextField("id", myDoc.getId(), Field.Store.YES);
            doc.add(id);
            Field content = new Field(textField, myDoc.getContent(), type);
            doc.add(content);

            if (indexWriter.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                indexWriter.addDocument(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the index
     * @param reader the index reader for the index
     */
    public static void printIndex(IndexReader reader) {
        try {
            Terms terms = MultiFields.getTerms(reader, "content");

            TermsEnum it = terms.iterator();
            //iterates through the terms of the lexicon
            while(it.next() != null) {
                // print the terms
                System.out.println(it.term().utf8ToString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param reader
     * @return
     * @throws Exception
     */
    public static Double[][] getSparseVecArray(IndexReader reader) throws Exception {
        Double[][] vecTable = new Double[0][0];
        Terms fieldTerms = MultiFields.getTerms(reader, "content");

        if (fieldTerms != null && fieldTerms.size() != -1) {
            IndexSearcher searcher = new IndexSearcher(reader);

            // return ALL docs via all-encompassing query
            ScoreDoc[] scoreDocs = searcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE).scoreDocs;
            vecTable = new Double[(int) fieldTerms.size()][scoreDocs.length];

            for (int j = 0; j < scoreDocs.length; j++) {
                Terms docTerms = reader.getTermVector(scoreDocs[j].doc, "content");

                Double[] vector = DocToDoubleVectorUtils.toSparseLocalFreqDoubleArray(docTerms, fieldTerms);
                for (int i = 0; i < vector.length; i++) {
                    vecTable[i][j] = vector[i];
                }
            }

        }
        return vecTable;
    }

    /**
     *
     * @param vecTable
     * @throws IOException
     */
    public static void writeSparseVecArrayToCSV(Double[][] vecTable, String file_name) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file_name))) {
            for (Double[] vec : vecTable){
                int size = vec.length;
                String[] str = new String[size];
                for(int i=0; i < size; i++) {
                    str[i] = vec[i].toString();
                }
                writer.writeNext(str);
            }
        }
    }

}
