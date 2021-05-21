import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;
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

import java.io.FileWriter;
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

    public static void printIndex(IndexReader reader) throws Exception{
        Terms terms = MultiFields.getTerms(reader, "content");

        TermsEnum it = terms.iterator();
        //iterates through the terms of the lexicon
        while(it.next() != null) {
            System.out.println(it.term().utf8ToString()); 		//prints the terms
        }

    }

    public static Double[][] getSparseVecArray(IndexReader reader) throws Exception{
        Double[][] vecTable = new Double[0][0];
        Terms fieldTerms = MultiFields.getTerms(reader, "content");

        if (fieldTerms != null && fieldTerms.size() != -1) {
            IndexSearcher searcher = new IndexSearcher(reader);

            ScoreDoc[] scoreDocs = searcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE).scoreDocs;
            vecTable = new Double[(int) fieldTerms.size()][scoreDocs.length];

            for (int j = 0; j < scoreDocs.length - 1; j++) {
                Terms docTerms = reader.getTermVector(scoreDocs[j].doc, "content");

                Double[] vector = DocToDoubleVectorUtils.toSparseLocalFreqDoubleArray(docTerms, fieldTerms);
                for (int i = 0; i < vector.length; i++) {
                    vecTable[i][j] = vector[i];
                }
            }

        }
        return vecTable;
    }

    public static void writeSparseVecArrayToCSV(Double[][] vecTable) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter("data.csv"))) {
            for (Double[] vec : vecTable){
                int size = vec.length;
                String[] str = new String[size];
                for(int i=0; i<size - 1; i++) {
                    str[i] = vec[i].toString();
                }
                writer.writeNext(str);
            }
        }
    }


    public static void printSVD(double[][] vecTable) {
        DoubleMatrix2D vec = new SparseDoubleMatrix2D(vecTable);

        SingularValueDecomposition s = new SingularValueDecomposition(vec);
        DoubleMatrix2D U = s.getU();
        DoubleMatrix2D S = s.getS();
        DoubleMatrix2D V = s.getV();

        System.out.println(U.toString());
        System.out.println(S.toString());
        System.out.println(V.toString());

    }

}
