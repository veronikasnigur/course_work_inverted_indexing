import java.io.Serializable;
import java.util.List;

public class SearchResultWrapper implements Serializable {
    private List<IndexEntry> results;

    public SearchResultWrapper(List<IndexEntry> results) {
        this.results = results;
    }

    public List<IndexEntry> getResults() {
        return results;
    }
}
