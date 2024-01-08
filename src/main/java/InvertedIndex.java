import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class InvertedIndex implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, List<IndexEntry>> invertedIndex;
    private boolean isIndexed;

    public InvertedIndex() {
        invertedIndex = new HashMap<>();
        isIndexed = false;
    }

    public void buildIndex(List<String> files) {
        for (String filePath : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                int position = 0;

                while ((line = reader.readLine()) != null) {
                    String[] words = line.split("\\s+");
                    for (String word : words) {
                        invertedIndex.computeIfAbsent(word, k -> new LinkedList<>()).add(new IndexEntry(filePath, position++));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        isIndexed = true;
    }

    public boolean isIndexed() {
        return isIndexed;
    }

    public List<IndexEntry> search(String query) {
        return invertedIndex.getOrDefault(query, new LinkedList<>());
    }
}
