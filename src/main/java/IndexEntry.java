import java.io.Serializable;
/**
 * Клас має конструктор для ініціалізації об'єкта, метод getFilePath для
 * отримання шляху до файлу та перевизначений метод toString, який надає
 * рядкове представлення об'єкта для зручного виводу.
 */
public class IndexEntry implements Serializable {
    private String filePath;// Шлях до файлу
    private int position;   // Позиція слова у файлі

    /**
     *
     * @param filePath шлях до файлу, де знаходиться слово.
     * @param position позиція(по порядку зліва-направо) слова у файлі
     */

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
