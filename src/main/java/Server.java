import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Server {
    private static final int PORT = 12345;
    private static final String BASE_DIRECTORY = "/Users/veronika_snigur/Downloads/cw/course_work_parallel_computing";
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                executorService.submit(() -> handleClient(clientSocket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {

            List<String> filesToIndex = Arrays.asList(
                    "aclImdb/test/neg", "aclImdb/test/pos",
                    "aclImdb/train/neg", "aclImdb/train/pos", "aclImdb/train/unsup"
            );
            List<String> absoluteFilePaths = filesToIndex.stream()
                    .map(dir -> BASE_DIRECTORY + File.separator + dir)
                    .flatMap(dir -> Arrays.stream(new File(dir).listFiles()))
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());

            InvertedIndex invertedIndex = new InvertedIndex();

            if (invertedIndex.isIndexed()) {
                System.out.println("Files are already indexed.");
            } else {
                System.out.println("Indexing files...");
                invertedIndex.buildIndex(absoluteFilePaths);
                System.out.println("Files indexed successfully.");
            }

            while (true) {
                // Read the search query from the client
                String query = (String) input.readObject();

                // Get the search results using the provided search query
                List<IndexEntry> searchResults = invertedIndex.getSearchResults(query);

                // Wrap the results in a SearchResultWrapper
                SearchResultWrapper resultWrapper = new SearchResultWrapper(searchResults);

                output.writeObject(resultWrapper);
                output.flush(); // Flush the output stream
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
