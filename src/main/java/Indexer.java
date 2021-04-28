import model.MyDoc;

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
}
