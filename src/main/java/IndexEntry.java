import java.io.Serializable;

public class IndexEntry implements Serializable {
    private String filePath;
    private int position;

    public IndexEntry(String filePath, int position) {
        this.filePath = filePath;
        this.position = position;
    }
    public String getFilePath() {
        return filePath;
    }
    @Override
    public String toString() {
        return "IndexEntry{" +
                "filePath='" + filePath + '\'' +
                ", position=" + position +
                '}';
    }
}
