import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InvertedIndex implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, List<IndexEntry>> invertedIndex;
    private boolean isIndexed;
    private final Object lock = new Object(); // Замок для синхронізації

    public InvertedIndex() {
        invertedIndex = new HashMap<>();
        isIndexed = false;
    }
    /**
     * Побудова інвертованого індексу для заданого списку файлів.
     * @param files Список шляхів до файлів, які слід індексувати.
     */
    public void buildIndex(List<String> files) {
//        long startTime = System.currentTimeMillis();
        for (String filePath : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                int position = 0;

                while ((line = reader.readLine()) != null) {
                    String[] words = line.split("\\s+");
                    for (String word : words) {
                        // Синхронізація доступу до invertedIndex
                        synchronized (lock) {
                            invertedIndex.computeIfAbsent(word, k -> new LinkedList<>()).add(new IndexEntry(filePath, position++));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        System.out.println("Time for execution with single thread: "+ (System.currentTimeMillis() - startTime)+" milliseconds\n");
        isIndexed = true;
    }
    /**
     * Перевірка, чи побудований індекс.
     * @return True, якщо індекс побудований, false - в іншому випадку.
     */
    public boolean isIndexed() {
        return isIndexed;
    }
    /**
     * Отримання результатів пошуку для заданого запиту.
     * @param query Пошуковий запит.
     * @return Список об'єктів IndexEntry, які представляють результати пошуку.
     */
    public List<IndexEntry> getSearchResults(String query) {
        // Тут немає потреби синхронізації, оскільки це лише читання
        return invertedIndex.getOrDefault(query, new LinkedList<>());
    }
}
