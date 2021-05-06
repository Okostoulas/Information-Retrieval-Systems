package model;

public class MyDoc {

    private String id;
    private String content;

    public MyDoc(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "MyDoc{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

}
