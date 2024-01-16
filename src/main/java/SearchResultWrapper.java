import java.io.Serializable;
import java.util.List;

/*
Клас SearchResultWrapper створений для обгортання результатів пошуку та передачі їх між сервером
 і клієнтом через потоки вводу/виводу об'єктів (ObjectInputStream і ObjectOutputStream) у зручному для передачі
 форматі.
*/

public class SearchResultWrapper implements Serializable {
    private List<IndexEntry> results;

    public SearchResultWrapper(List<IndexEntry> results) {
        this.results = results;
    }

    public List<IndexEntry> getResults() {
        return results;
    }
}
